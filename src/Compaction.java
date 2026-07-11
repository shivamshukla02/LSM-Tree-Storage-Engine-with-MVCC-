import java.io.*;
import java.util.*;

public class Compaction {
    public static void compact(List<String> inputFiles, String outputFile) throws IOException{
        TreeMap<String,String> merged = new TreeMap<>();

        for(String filePath : inputFiles){
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while((line = reader.readLine() ) != null){
                String[] parts = line.split(",");
                merged.put(parts[0],parts[1]);
                      }
            reader.close();
                  }
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        for(Map.Entry<String,String> entry : merged.entrySet()) {
            writer.write(entry.getKey() + "," + entry.getValue());
            writer.newLine();
               }
        writer.flush();
        writer.close();
            }
                }
