import java.io.*;
import java.util.*;

public class Compaction {

    public static void compact(List<String> inputFiles, String outputFile) throws IOException {
        TreeMap<String, String> merged = new TreeMap<>();

        for (String filePath : inputFiles) {
            DataInputStream in = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(filePath)));
            while (in.available() > 0) {
                int keyLen = in.readInt();
                byte[] keyBytes = new byte[keyLen];
                in.readFully(keyBytes);
                int valLen = in.readInt();
                byte[] valBytes = new byte[valLen];
                in.readFully(valBytes);
                merged.put(new String(keyBytes), new String(valBytes));
            }
            in.close();
        }

        DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(outputFile)));
        for (Map.Entry<String, String> entry : merged.entrySet()) {
            byte[] keyBytes = entry.getKey().getBytes();
            byte[] valBytes = entry.getValue().getBytes();
            out.writeInt(keyBytes.length);
            out.write(keyBytes);
            out.writeInt(valBytes.length);
            out.write(valBytes);
        }
        out.flush();
        out.close();
    }
}
