import java.net.StandardSocketOptions;
public class Main {
    public static void main(String arg[]){
MemTable memTable = new MemTable();
memTable.put("name", "shivam");
memTable.put("Project","LSM Engine");
System.out.println(memTable.get("name"));
System.out.println(memTable.get("Project"));}}
