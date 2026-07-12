import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        LRUCache cache = new LRUCache(3);
        cache.put("name", "shivam");
        cache.put("college", "PSIT");
        cache.put("project", "LSMEngine");

        System.out.println(cache.get("name"));

        cache.put("city", "kanpur");

        System.out.println(cache.get("college"));
        System.out.println(cache.get("name"));}}