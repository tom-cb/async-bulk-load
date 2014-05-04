
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OpTracker {

  ConcurrentHashMap<String, OpStatus> setTracker = new ConcurrentHashMap<String, OpStatus>();

  public void setScheduled(String k) {
    OpStatus opStat = setTracker.get(k);
    if (opStat == null) {
      opStat = new OpStatus();
    }
    
    opStat.setScheduled(); 
    setTracker.put(k, opStat);
  }

  public void setCompleted(String k) {
    OpStatus opStat = setTracker.get(k);
    opStat.setCompleted();
    setTracker.put(k, opStat);
  }

  public void setRescheduled(String k) {
    OpStatus opStat = setTracker.get(k);
    opStat.setRescheduled();
    setTracker.put(k, opStat);
  }

  public void setOnCompleted(String k) {
    OpStatus opStat = setTracker.get(k);
    opStat.setOnCompleted();
    setTracker.put(k, opStat);
  }


  public void printTracker() {
   String str = "";
   Iterator<Map.Entry<String, OpStatus>> it = setTracker.entrySet().iterator();   
   while(it.hasNext()) {
     Map.Entry mapEntry = (Map.Entry) it.next();
     String res = ( (OpStatus)(mapEntry.getValue()) ).toString();
  
     if (res != "") { 
       System.out.println("Key: " + mapEntry.getKey() + " " + res);
     }
   }
  }
}
