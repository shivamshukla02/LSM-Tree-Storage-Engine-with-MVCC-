import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        LSMEngine engine = new LSMEngine();

        engine.put("name", "shivam");
        long snapshot = engine.currentSnapshot();
        engine.put("name", "shivam_updated");

        System.out.println(engine.getSnapshot("name", snapshot));
        System.out.println(engine.getSnapshot("name", engine.currentSnapshot()));
    }
}