import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WriteAheadLog {
    private BufferedWriter writer;
    public WriteAheadLog(String filepath ) throws IOException{
        this.writer= new BufferedWriter(new FileWriter(filepath,true));}
public void append(String key, String value) throws IOException{
        writer.write(key+","+value);
        writer.newLine();
        writer.flush();}
}
