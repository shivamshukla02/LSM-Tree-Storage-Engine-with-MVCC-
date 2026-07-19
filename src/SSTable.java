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

    public static String read(String filepath, String searchkey) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(filepath));
        String line;
        while((line = reader.readLine())!= null){
            String[] parts =line.split(",");
            if(parts[0].equals(searchkey)){
                reader.close();
                return parts[1];
            }
        }
        reader.close();
        return null;
    }
}
