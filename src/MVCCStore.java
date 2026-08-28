import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

    public class MVCCStore {
        private final ConcurrentHashMap<String, ConcurrentSkipListMap<Long, String>> store;
        private final AtomicLong lamportClock;

        public MVCCStore() {
            this.store = new ConcurrentHashMap<>();
            this.lamportClock = new AtomicLong(0);
        }

        public long put(String key, String value) {
            long timestamp = lamportClock.incrementAndGet();
            store.computeIfAbsent(key, k -> new ConcurrentSkipListMap<>())
                    .put(timestamp, value);
            return timestamp;
        }

        public String get(String key, long snapshotTime) {
            ConcurrentSkipListMap<Long, String> versions = store.get(key);
            if (versions == null) return null;
            Map.Entry<Long, String> entry = versions.floorEntry(snapshotTime);
            return entry == null ? null : entry.getValue();
        }

        public long getSnapshot() {
            return lamportClock.get();
        }
    }

