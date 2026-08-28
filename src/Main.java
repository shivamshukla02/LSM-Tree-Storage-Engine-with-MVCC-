import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        MVCCStore mvcc = new MVCCStore();

        mvcc.put("name", "shivam");
        long snapshot = mvcc.getSnapshot();
        mvcc.put("name", "shivam_updated");

        System.out.println(mvcc.get("name", snapshot));
        System.out.println(mvcc.get("name", mvcc.getSnapshot()));
    }
}