import java.util.concurrent.ConcurrentSkipListMap;

public class MemTable {
private final ConcurrentSkipListMap<String, String> table;
public MemTable(){
    this.table =  new ConcurrentSkipListMap<>();}
    public void put(String key, String value){
    table.put(key,value);}
    public String get(String key){
    return table.get(key);} }



