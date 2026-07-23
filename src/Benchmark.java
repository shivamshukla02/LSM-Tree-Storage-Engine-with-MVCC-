import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Benchmark {

    public static void runWriteBenchmark(int numOperations) throws IOException {
        LSMEngine engine = new LSMEngine();
        long[] latencies = new long[numOperations];

        long start = System.nanoTime();

        for (int i = 0; i < numOperations; i++) {
            long opStart = System.nanoTime();
            engine.put("key" + i, "value" + i);
            long opEnd = System.nanoTime();
            latencies[i] = opEnd - opStart;
        }

        long end = System.nanoTime();
        double totalSeconds = (end - start) / 1_000_000_000.0;
        double writesPerSec = numOperations / totalSeconds;

        List<Long> sorted = new ArrayList<>();
        for (long l : latencies) sorted.add(l);
        Collections.sort(sorted);

        long p99 = sorted.get((int) (numOperations * 0.99));
        long p99Micros = p99 / 1000;

        System.out.println("operations: " + numOperations);
        System.out.printf("writes/sec: %.0f%n", writesPerSec);
        System.out.println("p99 write latency: " + p99Micros + " µs");
    }
}