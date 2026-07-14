import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        WriteAheadLog wal = new WriteAheadLog("wal.log");
        LRUCache cache = new LRUCache(10);
        MemTable memTable = new MemTable(wal, cache);

        memTable.put("name", "shivam");
        memTable.put("college", "PSIT");
        memTable.put("project", "LSMEngine");

        System.out.println(memTable.get("name"));
        System.out.println(memTable.get("college"));
        System.out.println(memTable.get("randomkey"));}}