import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache {
    private final int capacity;
    private final LinkedHashMap<String,String> cache;

    public LRUCache(int capacity){
        this.capacity = capacity;
        this.cache = new LinkedHashMap<>(capacity,0.75f,true){
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size()> capacity;
            }
        };
    }
    public void put(String key, String value){
        cache.put(key,value);
    }
    public String get(String key){
        return cache.getOrDefault(key,null);
    }
}
