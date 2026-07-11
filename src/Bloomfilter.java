import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import java.nio.charset.Charset;

public class Bloomfilter {
    private final com.google.common.hash.BloomFilter<String> filter;

    public Bloomfilter(int expectedInsertions) {
        this.filter = com.google.common.hash.BloomFilter.create(
                Funnels.stringFunnel(Charset.defaultCharset()),
                expectedInsertions,
                0.01
        );
    }

    public void add(String key) {
        filter.put(key);
    }

    public boolean mightContain(String key) {
        return filter.mightContain(key);
    }
}