import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        WriteAheadLog wal = new WriteAheadLog("wal.log");
        LRUCache cache = new LRUCache(10);
        MemTable memTable = new MemTable(wal, cache);

        memTable.put("name", "shivam");
        memTable.put("college", "PSIT");
        SSTable.flush(memTable.getTable(), "sstable_1.sst");

        Bloomfilter bloom = new Bloomfilter(100);
        bloom.add("name");
        bloom.add("college");

        String searchKey = "college";

        if (bloom.mightContain(searchKey)) {
            String result = SSTable.read("sstable_1.sst", searchKey);
            System.out.println("found: " + result);
        } else {
            System.out.println("bloom filter skipped this SSTable");
        }}}