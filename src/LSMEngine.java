import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LSMEngine {
    private MemTable memTable;
    private final WriteAheadLog wal;
    private final LRUCache cache;
    private final Bloomfilter bloom;
    private final List<String> sstableFiles;
    private int sstableCount;
    private static final int MEMTABLE_LIMIT = 3;

    public LSMEngine() throws IOException {
        this.wal = new WriteAheadLog("wal.log");
        this.cache = new LRUCache(100);
        this.bloom = new Bloomfilter(10000);
        this.sstableFiles = new ArrayList<>();
        this.sstableCount = 0;
        this.memTable = new MemTable(wal, cache);
    }

    public void put(String key, String value) throws IOException {
        memTable.put(key, value);
        bloom.add(key);
        if (memTable.getTable().size() >= MEMTABLE_LIMIT) {
            flush();
        }
    }

    private void flush() throws IOException {
        String fileName = "sstable_" + sstableCount++ + ".sst";
        SSTable.flush(memTable.getTable(), fileName);
        sstableFiles.add(fileName);
        memTable = new MemTable(wal, cache);
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
}