# LSM-Tree-Storage-Engine-with-MVCC-
LSM stands for Log-Structured Merge Tree. Invented by Patrick O'Neil in 1996. Powers RocksDB, LevelDB, Cassandra and more. The key insight: sequential disk writes are 100× faster than random writes.  Old databases (B+Tree) update data in-place, requiring random writes everywhere. It avoids random writes entirely by always appending sequentially.
