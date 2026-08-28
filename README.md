# LSM-Tree Storage Engine with MVCC

> A high-performance, production-inspired Log-Structured Merge Tree (LSM) storage engine built from scratch in Java 17 — featuring Write-Ahead Logging, Bloom Filters, Sharded LRU Cache, Adaptive Compaction, and Multi-Version Concurrency Control (MVCC).

---

## Benchmark Results

| Metric | Result | Hardware |
|---|---|---|
| Random Writes/sec | **117,000+** | Ryzen 5 5600H, NVMe SSD |
| p99 Write Latency | **178 µs** | 8 concurrent threads |
| Space Amplification | **~1.0x** | Post single-level compaction |
| BloomFilter False Positive Rate | **~1%** | 10 bits/key |
| Test Coverage | **5/5 JUnit tests passing** | All components |

---

## Why LSM Tree?

Traditional databases (B+Tree) update data **in-place** — requiring expensive random disk writes everywhere. LSM Trees solve this by **never doing random writes**. Every write is sequential, which is 100× faster on both SSDs and HDDs.

```
B+Tree:  Write → Find position on disk → Random write → Slow ❌
LSM:     Write → Append to WAL → RAM (MemTable) → Sequential flush → Fast ✅
```

LSM powers: **RocksDB, LevelDB, Apache Cassandra, ScyllaDB, TiKV, InfluxDB**

---

## Architecture Overview

```mermaid
flowchart TD
    Client(["👤 Client"])

    subgraph WP["✍️ Write Path"]
        direction TB
        WAL["📋 WriteAheadLog\nAppend-only crash recovery log\nFlushed to disk before MemTable"]
        MEM["🧠 MemTable\nConcurrentSkipListMap\nSorted in-memory store"]
        MVCC["🔢 MVCCStore\nLamport Timestamps\nSnapshot Isolation"]
    end

    subgraph FP["💾 Flush Pipeline"]
        direction TB
        SST["📁 SSTable\nBinary sorted on-disk file\nImmutable after write"]
        COMP["⚙️ Compaction\nAdaptive throttled background merge\nEliminates duplicate keys"]
    end

    subgraph RP["📖 Read Path"]
        direction TB
        LRU["⚡ ShardedLRU Cache\n8 independent shards\nO(1) hot key lookup"]
        BF["🔍 BloomFilter\n10 bits/key — 1% FPR\nSkips 90%+ SSTable reads"]
    end

    Client -->|"put(key, value)"| WAL
    WAL -->|"after WAL write"| MEM
    MEM -->|"put into"| MVCC
    MEM -->|"flush when full\n(MEMTABLE_LIMIT = 1000)"| SST
    SST -->|"3+ SSTables trigger"| COMP
    COMP -->|"merged SSTable"| SST

    Client -->|"get(key)"| LRU
    LRU -->|"cache miss"| MEM
    MEM -->|"not in memory"| BF
    BF -->|"mightContain = true\ncheck SSTable"| SST
    BF -->|"false = skip file\nsaved disk read"| Client
    SST -->|"found value"| Client

    Client -->|"getSnapshot(key, time)"| MVCC
    MVCC -->|"version at snapshot time"| Client
```

---

## Write Path — Step by Step

```mermaid
sequenceDiagram
    participant C as Client
    participant WAL as WriteAheadLog
    participant MEM as MemTable
    participant BF as BloomFilter
    participant MVCC as MVCCStore
    participant SST as SSTable
    participant COMP as Compaction

    C->>WAL: 1. append(key, value) — crash safety first
    WAL-->>WAL: write to disk sequentially
    WAL->>MEM: 2. put(key, value) — fast RAM write
    MEM->>BF: 3. bloom.add(key)
    MEM->>MVCC: 4. mvcc.put(key, value) — assign Lamport timestamp
    
    alt MemTable full (size >= 1000)
        MEM->>SST: 5. flush() — write sorted binary file to disk
        SST-->>WAL: 6. wal.clear() — WAL no longer needed
        
        alt 3+ SSTables exist
            SST->>COMP: 7. compact() in background thread
            COMP-->>SST: 8. merged SSTable, old files deleted
        end
    end
```

---

## Read Path — Step by Step

```mermaid
sequenceDiagram
    participant C as Client
    participant LRU as ShardedLRU Cache
    participant MEM as MemTable
    participant BF as BloomFilter
    participant SST as SSTable

    C->>LRU: 1. cache.get(key)
    
    alt Cache HIT
        LRU-->>C: return value instantly (O(1), no disk)
    else Cache MISS
        LRU->>MEM: 2. memTable.get(key)
        
        alt Found in MemTable
            MEM-->>C: return value (RAM, fast)
        else Not in MemTable
            MEM->>BF: 3. bloom.mightContain(key)
            
            alt BloomFilter returns FALSE
                BF-->>C: return null (key definitely absent, 0 disk reads)
            else BloomFilter returns TRUE
                BF->>SST: 4. SSTable.read() — newest to oldest
                SST-->>C: return value or null
            end
        end
    end
```

---

## Component Deep Dive

### MemTable — In-Memory Write Buffer

```mermaid
flowchart LR
    W1["write: name=shivam"] --> SKL
    W2["write: city=kanpur"] --> SKL
    W3["write: age=20"] --> SKL
    
    subgraph SKL["ConcurrentSkipListMap (sorted)"]
        A["age=20"] --> B["city=kanpur"] --> C["name=shivam"]
    end
    
    SKL -->|"flush when full"| DISK["SSTable on disk\n(keys already sorted)"]
```

**Why ConcurrentSkipListMap?**
- Keeps keys **sorted automatically** → SSTable flush needs no sorting step
- **Thread-safe** for concurrent writes without full locking
- O(log n) insert, search, delete

---

### WriteAheadLog — Crash Recovery

```mermaid
flowchart TD
    W["engine.put(key, value)"]
    
    W -->|"Step 1 — ALWAYS first"| DISK["wal.log on disk\nkey1,value1\nkey2,value2\n..."]
    W -->|"Step 2 — after WAL"| RAM["MemTable in RAM"]
    
    CRASH["💥 Power Loss / Crash"]
    RESTART["🔄 Engine Restart"]
    REPLAY["WAL Replay\nreplayWAL()"]
    RECOVER["MemTable rebuilt\nfrom WAL log"]
    
    RAM -->|"crash before flush"| CRASH
    DISK -->|"survives crash"| RESTART
    RESTART --> REPLAY
    REPLAY --> RECOVER
    
    FLUSH["Successful flush to SSTable"]
    CLEAR["wal.clear()\nWAL reset — data safe on disk"]
    
    RAM -->|"MemTable full"| FLUSH
    FLUSH --> CLEAR
```

---

### BloomFilter — Read Optimization

```mermaid
flowchart TD
    KEY["search: 'username'"]
    BF["BloomFilter\n10 bits/key\nk=7 hash functions"]
    
    KEY --> BF
    
    BF -->|"returns FALSE\n= key DEFINITELY absent"| SKIP["Skip SSTable\n0 disk reads saved ✅"]
    BF -->|"returns TRUE\n= key POSSIBLY present"| READ["Read SSTable\ncheck if key exists"]
    
    READ -->|"key found"| HIT["Return value ✅"]
    READ -->|"key not found\n(false positive)"| MISS["Return null\n1 unnecessary read (acceptable)"]
```

**Key property:** BloomFilter NEVER gives false negatives. If it says NO, the key is 100% absent.

---

### ShardedLRU Cache — Concurrent Hot Key Cache

```mermaid
flowchart TD
    KEY["get(key)"]
    HASH["hash(key) % 8\ndetermine shard"]
    
    KEY --> HASH
    
    HASH --> S0["Shard 0\nLRU Map\nown lock"]
    HASH --> S1["Shard 1\nLRU Map\nown lock"]
    HASH --> S2["Shard 2\nLRU Map\nown lock"]
    HASH --> S7["Shard 7\nLRU Map\nown lock"]
    
    S0 & S1 & S2 & S7 -->|"8 threads → 8 shards\nno contention"| RESULT["Return value or null"]
```

**Why sharded?** Single LRU = 1 lock = 8 threads wait in line. Sharded LRU = 8 locks = threads work in parallel.

---

### MVCC — Multi-Version Concurrency Control

```mermaid
sequenceDiagram
    participant T1 as Thread 1 (Reader)
    participant T2 as Thread 2 (Writer)
    participant MVCC as MVCCStore

    T2->>MVCC: put("name", "shivam") → timestamp=1
    T1->>MVCC: snapshot = getSnapshot() → snapshot=1
    T2->>MVCC: put("name", "shivam_v2") → timestamp=2
    T1->>MVCC: get("name", snapshot=1) → "shivam"
    Note over T1: Reader sees consistent snapshot
    Note over T2: Writer doesn't block reader
```

---

### Compaction — Background Merge

```mermaid
flowchart LR
    S1["sstable_0.sst\nname=shivam\ncity=kanpur"]
    S2["sstable_1.sst\nname=shivam_v2\nage=20"]
    S3["sstable_2.sst\nproject=LSMEngine"]
    
    S1 & S2 & S3 -->|"background thread\nadaptive throttle"| MERGE["TreeMap merge\nlatest value wins"]
    
    MERGE --> OUT["sstable_compacted.sst\nage=20\ncity=kanpur\nname=shivam_v2\nproject=LSMEngine"]
    
    OUT -->|"old files deleted"| CLEAN["Space reclaimed\n~1.0x amplification"]
```

---

## Project Structure

```
LSMEngine/
├── src/
│   ├── Main.java              # Entry point + benchmark runner
│   ├── LSMEngine.java         # Core engine — orchestrates all components
│   ├── MemTable.java          # In-memory sorted write buffer
│   ├── WriteAheadLog.java     # Crash-safe append-only log
│   ├── SSTable.java           # Binary on-disk sorted file (flush + read)
│   ├── BloomFilter.java       # Probabilistic key existence check
│   ├── ShardedLRUCache.java   # 8-shard concurrent LRU cache
│   ├── Compaction.java        # Background adaptive SSTable merge
│   ├── MVCCStore.java         # Lamport timestamp versioning
│   ├── Benchmark.java         # 8-thread write throughput + p99 measurement
│   └── LSMEngineTest.java     # JUnit 4 test suite
├── .gitignore                 # Ignores .sst and .log generated files
└── README.md
```

---

## Running Locally

### Prerequisites

| Tool | Version | Download |
|---|---|---|
| Java JDK | 17+ | https://adoptium.net |
| IntelliJ IDEA | Any | https://jetbrains.com/idea |
| Git | Any | https://git-scm.com |

### Setup Steps

**1. Clone the repository**
```bash
git clone https://github.com/shivamshukla02/LSMEngine.git
cd LSMEngine
```

**2. Open in IntelliJ**
- File → Open → select the `LSMEngine` folder
- IntelliJ will detect the project automatically

**3. Add dependencies via IntelliJ**
- File → Project Structure → Libraries → `+` → From Maven
- Add: `com.google.guava:guava:32.1.3-jre`
- Add: `junit:junit:4.13.2`

**4. Run the benchmark**
- Open `Main.java`
- Click the green ▶ Run button
- Expected output:
```
operations: 100000
writes/sec: 117000+
p99 write latency: ~178 µs
```

**5. Run tests**
- Right click `LSMEngineTest.java` → Run
- Expected: 5/5 tests passing ✅

### Basic Usage

```java
// Create engine
LSMEngine engine = new LSMEngine();

// Write
engine.put("username", "shivam");
engine.put("project", "LSMEngine");

// Read
String value = engine.get("username"); // "shivam"

// Snapshot read (MVCC)
engine.put("version", "v1");
long snapshot = engine.currentSnapshot();
engine.put("version", "v2");

engine.getSnapshot("version", snapshot);          // "v1"
engine.getSnapshot("version", engine.currentSnapshot()); // "v2"

// Crash recovery
engine.replayWAL(); // call on restart to rebuild MemTable

// Space amplification
engine.measureSpaceAmplification();
```

---

## Design Decisions & Tradeoffs

| Decision | Why | Tradeoff |
|---|---|---|
| ConcurrentSkipListMap for MemTable | Sorted + thread-safe + O(log n) | Slower than HashMap for single-thread |
| WAL before MemTable | Crash safety — survive power loss | Extra disk write per batch |
| Binary SSTable format | 10× faster than text parsing | Not human-readable |
| Sharded LRU (8 shards) | Reduces lock contention 8× | More memory overhead |
| Async compaction | Writes never block on compaction | Temporary read amplification |
| MVCC with Lamport clocks | Works across machines, no clock drift | More memory per key (multiple versions) |
| BloomFilter 10 bits/key | Eliminates 90%+ unnecessary reads | ~1% false positive rate |


## Future Improvements

- [ ] Binary search index within SSTable for O(log n) key lookup
- [ ] Leveled compaction (L0→L1→L2) for better read amplification
- [ ] Memory-mapped files (mmap) for SSTable access
- [ ] Prometheus metrics integration for real-time monitoring
- [ ] gRPC server to expose as a network key-value store
- [ ] Bloom filter persistence across restarts

---

## Author

**Shivam Shukla** — B.Tech CSE, PSIT Kanpur (2025–2029)

[![GitHub](https://img.shields.io/badge/GitHub-shivamshukla02-black)](https://github.com/shivamshukla02)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-shivam--shukla-blue)](https://www.linkedin.com/in/shivam-shukla-a944a13b4/)
[![LeetCode](https://img.shields.io/badge/LeetCode-shivam__shukla__02-orange)](https://leetcode.com/u/shivam_shukla_02/)
