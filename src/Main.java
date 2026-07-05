import java.io.IOException;
public class Main {
    public static void main(String[] args) throws IOException {
        WriteAheadLog wal = new WriteAheadLog ("wal.log");
        wal.append("name", "shivam");
        wal.append("project", "LSMEngine");
        System.out.println("WAL write done");}}
