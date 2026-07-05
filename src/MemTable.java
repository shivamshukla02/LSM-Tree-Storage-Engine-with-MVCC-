import java.io.IOException;
import java.util.concurrent.ConcurrentSkipListMap;

public class MemTable{
    private final ConcurrentSkipListMap<String,String>table;
    private final WriteAheadLog wal;

    public MemTable (WriteAheadLog wal){
        this.table = new ConcurrentSkipListMap<>();
        this.wal = wal;
    }

    public void put(String key, String value ) throws IOException{
        wal.append(key, value);
        table.put(key, value);

    }

    public String get(String key){
        return table.get(key);
    }
    public ConcurrentSkipListMap<String, String> getTable() {
        return table;
    }
}

