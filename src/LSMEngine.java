import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LSMEngine {
    private MemTable memTable;
    private final WriteAheadLog wal;
    private final ShardedLRUCache cache;
    private final Bloomfilter bloom;
    private final MVCCStore mvcc;
    private final List<String> sstableFiles;
    private int sstableCount;
    private static final int MEMTABLE_LIMIT = 1000;
    private volatile long lastFlushTime = System.currentTimeMillis();

    public LSMEngine() throws IOException {
        this.wal = new WriteAheadLog("wal.log");
        this.cache = new ShardedLRUCache(10000, 8);
        this.bloom = new Bloomfilter(10000, 10);
        this.mvcc = new MVCCStore();
        this.sstableFiles = new ArrayList<>();
        this.sstableCount = 0;
        this.memTable = new MemTable(wal, cache);
    }

    public synchronized void put(String key, String value) throws IOException {
        wal.append(key, value);
        memTable.put(key, value);
        bloom.add(key);
        mvcc.put(key, value);
        if (memTable.getTable().size() >= MEMTABLE_LIMIT) {
            flush();
        }
    }

    private void flush() throws IOException {
        String fileName = "sstable_" + sstableCount++ + ".sst";
        SSTable.flush(memTable.getTable(), fileName);
        sstableFiles.add(fileName);
        memTable = new MemTable(wal, cache);
        wal.flushWAL();
        wal.clear();
        if (sstableFiles.size() >= 3) {
            List<String> toCompact = new ArrayList<>(sstableFiles);
            new Thread(() -> {
                try {
                    long timeSinceLastFlush = System.currentTimeMillis() - lastFlushTime;
                    long throttleMs;
                    if (timeSinceLastFlush < 100) {
                        throttleMs = 0;
                    } else if (timeSinceLastFlush < 500) {
                        throttleMs = 1;
                    } else {
                        throttleMs = 5;
                    }
                    compact(toCompact, throttleMs);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        lastFlushTime = System.currentTimeMillis();
        System.out.println("flushed to " + fileName);
    }

    public String get(String key) throws IOException {
        String cached = cache.get(key);
        if (cached != null) return cached;

        String fromMem = memTable.getTable().get(key);
        if (fromMem != null) return fromMem;

        if (!bloom.mightContain(key)) return null;

        for (int i = sstableFiles.size() - 1; i >= 0; i--) {
            String result = SSTable.read(sstableFiles.get(i), key);
            if (result != null) return result;
        }
        return null;
    }

    public String getSnapshot(String key, long snapshotTime) {
        return mvcc.get(key, snapshotTime);
    }

    public long currentSnapshot() {
        return mvcc.getSnapshot();
    }

    public void compact(List<String> filesToCompact, long throttleMs) throws IOException {
        if (filesToCompact.size() < 2) return;
        String outputFile = "sstable_compacted_" + sstableCount++ + ".sst";
        Compaction.compact(filesToCompact, outputFile, throttleMs);
        synchronized (sstableFiles) {
            sstableFiles.clear();
            sstableFiles.add(outputFile);
        }
        System.out.println("compacted into " + outputFile);
    }

    public void replayWAL() throws IOException {
        List<String[]> entries = wal.readAll();
        for (String[] entry : entries) {
            memTable.getTable().put(entry[0], entry[1]);
            bloom.add(entry[0]);
            cache.put(entry[0], entry[1]);
        }
        System.out.println("replayed " + entries.size() + " entries from WAL");
    }
}