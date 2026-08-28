import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        LSMEngine engine = new LSMEngine();

        for (int i = 0; i < 3000; i++) {
            engine.put("key" + i, "value" + i);
        }

        engine.measureSpaceAmplification();

        for (int i = 0; i < 2000; i++) {
            engine.put("key" + i, "value_updated_" + i);
        }

        Thread.sleep(2000);
        engine.measureSpaceAmplification();
    }
}