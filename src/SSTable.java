import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class SSTable {

    public static void flush(ConcurrentSkipListMap<String,String>table, String filepath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
        for (Map.Entry<String, String> entry : table.entrySet()) {
            writer.write(entry.getKey() + "," + entry.getValue());
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }
}
