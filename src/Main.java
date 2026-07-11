import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        WriteAheadLog wal = new WriteAheadLog("wal.log");
        MemTable memTable = new MemTable(wal);

        memTable.put("name", "shivam");
        memTable.put("college", "PSIT");
        SSTable.flush(memTable.getTable(), "sstable_1.sst");

        MemTable memTable2 = new MemTable(wal);
        memTable2.put("name", "shivam_updated");
        memTable2.put("project", "LSMEngine");
        SSTable.flush(memTable2.getTable(), "sstable_2.sst");

        List<String> files = List.of("sstable_1.sst", "sstable_2.sst");
        Compaction.compact(files, "sstable_compacted.sst");
        System.out.println("compaction done");}}
