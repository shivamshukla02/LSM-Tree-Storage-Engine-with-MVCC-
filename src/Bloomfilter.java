import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import java.nio.charset.Charset;

public class Bloomfilter {
    private final com.google.common.hash.BloomFilter<String> filter;
    private final int bitsPerKey;

    public Bloomfilter(int expectedInsertions, int bitsPerKey) {
        this.bitsPerKey = bitsPerKey;
        double fpp = Math.pow(0.6185, bitsPerKey);
        this.filter = com.google.common.hash.BloomFilter.create(
                Funnels.stringFunnel(Charset.defaultCharset()),
                expectedInsertions,
                fpp
        );
    }

    public void add(String key) {
        filter.put(key);
    }

    public boolean mightContain(String key) {
        return filter.mightContain(key);
    }

    public int getBitsPerKey() {
        return bitsPerKey;
    }
}