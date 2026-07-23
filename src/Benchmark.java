import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class Benchmark {

    public static void runWriteBenchmark(int numOperations) throws IOException, InterruptedException {
        LSMEngine engine = new LSMEngine();
        long[] latencies = new long[numOperations];
        int threads = 8;
        int opsPerThread = numOperations / threads;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        long start = System.nanoTime();

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            pool.submit(() -> {
                int from = threadId * opsPerThread;
                int to = from + opsPerThread;
                for (int i = from; i < to; i++) {
                    try {
                        long opStart = System.nanoTime();
                        engine.put("key" + i, "value" + i);
                        latencies[i] = System.nanoTime() - opStart;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                latch.countDown();
            });
        }

        latch.await();
        long end = System.nanoTime();
        pool.shutdown();

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