import java.io.IOException;
public class Main {
    public static void main(String[] args) throws IOException {
        WriteAheadLog wal = new WriteAheadLog("wal.log");
        MemTable memTable = new MemTable(wal);
        memTable.put("name", "shivam");
        memTable.put("project", "LSMEngine");
        memTable.put("college", "PSIT");

        SSTable.flush(memTable.getTable(), "sstable_1.sst");
        System.out.println("flushed to disk");}}
