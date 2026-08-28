import java.util.LinkedHashMap;
import java.util.Map;

public class ShardedLRUCache {
    private final int numShards;
    private final int shardCapacity;
    private final LinkedHashMap<String, String>[] shards;
    private final Object[] locks;

    @SuppressWarnings("unchecked")
    public ShardedLRUCache(int totalCapacity, int numShards) {
        this.numShards = numShards;
        this.shardCapacity = (totalCapacity + numShards - 1) / numShards;
        this.shards = new LinkedHashMap[numShards];
        this.locks = new Object[numShards];

        for (int i = 0; i < numShards; i++) {
            final int cap = shardCapacity;
            locks[i] = new Object();
            shards[i] = new LinkedHashMap<>(cap, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() > cap;
                }
            };
        }
    }

    private int getShard(String key) {
        return Math.floorMod(key.hashCode(), numShards);
    }

    public void put(String key, String value) {
        int shard = getShard(key);
        synchronized (locks[shard]) {
            shards[shard].put(key, value);
        }
    }

    public String get(String key) {
        int shard = getShard(key);
        synchronized (locks[shard]) {
            return shards[shard].getOrDefault(key, null);
        }
    }
}
