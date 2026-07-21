import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        LSMEngine engine = new LSMEngine();
        engine.replayWAL();

        engine.put("name", "shivam");
        engine.put("college", "PSIT");
        engine.put("project", "LSMEngine");

        System.out.println(engine.get("name"));
        System.out.println(engine.get("college"));}}