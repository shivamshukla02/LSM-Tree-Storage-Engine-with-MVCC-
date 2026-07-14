import java.io.IOException;
import java.util.concurrent.ConcurrentSkipListMap;

public class MemTable {
    private final ConcurrentSkipListMap<String, String> table;
    private final WriteAheadLog wal;
    private final LRUCache cache;

    public MemTable(WriteAheadLog wal, LRUCache cache) {
        this.table = new ConcurrentSkipListMap<>();
        this.wal = wal;
        this.cache = cache;
    }

    public void put(String key, String value) throws IOException {
        wal.append(key, value);
        table.put(key, value);
        cache.put(key, value);
    }

    public String get(String key) {
        String cached = cache.get(key);
        if (cached != null) return cached;
        return table.get(key);
    }

    public ConcurrentSkipListMap<String, String> getTable() {
        return table;
    }
}

