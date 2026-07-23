import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class SSTable {

    public static void flush(ConcurrentSkipListMap<String, String> table, String filePath) throws IOException {
        DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(filePath)));
        for (Map.Entry<String, String> entry : table.entrySet()) {
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

    public static String read(String filePath, String searchKey) throws IOException {
        DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(filePath)));
        while (in.available() > 0) {
            int keyLen = in.readInt();
            byte[] keyBytes = new byte[keyLen];
            in.readFully(keyBytes);
            int valLen = in.readInt();
            byte[] valBytes = new byte[valLen];
            in.readFully(valBytes);
            if (new String(keyBytes).equals(searchKey)) {
                in.close();
                return new String(valBytes);
            }
        }
        in.close();
        return null;
    }
}
