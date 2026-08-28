import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;

public class LSMEngineTest {

    @Test
    public void testPutAndGet() throws IOException {
        LSMEngine engine = new LSMEngine();
        engine.put("name", "shivam");
        assertEquals("shivam", engine.get("name"));
    }

    @Test
    public void testMissingKey() throws IOException {
        LSMEngine engine = new LSMEngine();
        assertNull(engine.get("nonexistent"));
    }

    @Test
    public void testOverwrite() throws IOException {
        LSMEngine engine = new LSMEngine();
        engine.put("name", "shivam");
        engine.put("name", "shivam_updated");
        assertEquals("shivam_updated", engine.get("name"));
    }

    @Test
    public void testMVCCSnapshot() throws IOException {
        LSMEngine engine = new LSMEngine();
        engine.put("name", "shivam");
        long snapshot = engine.currentSnapshot();
        engine.put("name", "shivam_updated");
        assertEquals("shivam", engine.getSnapshot("name", snapshot));
        assertEquals("shivam_updated", engine.getSnapshot("name", engine.currentSnapshot()));
    }

    @Test
    public void testBloomFilter() throws IOException {
        LSMEngine engine = new LSMEngine();
        engine.put("exists", "yes");
        assertNull(engine.get("definitelynothere12345"));
    }
}