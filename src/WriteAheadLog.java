import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.List;

public class WriteAheadLog {
    private BufferedWriter writer;
    public WriteAheadLog(String filepath ) throws IOException{
        this.writer= new BufferedWriter(new FileWriter(filepath,true));}
    public void append(String key, String value) throws IOException {
        writer.write(key + "," + value);
        writer.newLine();
    }

    public void flushWAL() throws IOException {
        writer.flush();
    }
    public List<String[]> readAll() throws IOException {
        List<String[]> entries = new ArrayList<>();
        File file = new File(writer.toString());
        BufferedReader reader = new BufferedReader(new FileReader("wal.log"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length == 2) {
                entries.add(parts);
            }
        }
        reader.close();
        return entries;
    }
    public void clear() throws IOException {
        writer.close();
        writer = new BufferedWriter(new FileWriter("wal.log", false));
    }

}
