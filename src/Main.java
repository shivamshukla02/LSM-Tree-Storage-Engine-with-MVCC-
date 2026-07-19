import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        LSMEngine engine = new LSMEngine();

        engine.put("name", "shivam");
        engine.put("college", "PSIT");
        engine.put("project", "LSMEngine");
        engine.put("city", "kanpur");
        engine.put("year", "2025");

        System.out.println(engine.get("name"));
        System.out.println(engine.get("city"));
        System.out.println(engine.get("missing"));}}