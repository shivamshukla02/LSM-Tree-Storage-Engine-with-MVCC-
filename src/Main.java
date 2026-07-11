import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Bloomfilter bloom = new Bloomfilter(100);
        bloom.add("name");
        bloom.add("college");

        System.out.println(bloom.mightContain("name"));
        System.out.println(bloom.mightContain("college"));
        System.out.println(bloom.mightContain("randomkey"));}}
