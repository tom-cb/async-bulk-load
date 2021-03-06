
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

  public void setRetried(String k) {
    OpStatus opStat = setTracker.get(k);
    opStat.setRetried();
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
   int totalOpsScheduled = 0;
   int totalOpsCompleted = 0;
   int totalOpsRetried = 0;
   int totalOpsRescheduled = 0;

   while(it.hasNext()) {
     Map.Entry mapEntry = (Map.Entry) it.next();
     totalOpsCompleted += ((OpStatus)mapEntry.getValue()).completed;
     totalOpsScheduled += ((OpStatus)mapEntry.getValue()).scheduled;
     totalOpsRescheduled += ((OpStatus)mapEntry.getValue()).rescheduled;
     totalOpsRetried += ((OpStatus)mapEntry.getValue()).retried;

     String res = ( (OpStatus)(mapEntry.getValue()) ).toString();
  
     if (res != "") { 
       System.out.println("Key: " + mapEntry.getKey() + " " + res);
     }
   }

   System.out.println("Total ops scheduled: " + totalOpsScheduled);
   System.out.println("Total ops complete: " + totalOpsCompleted);
   System.out.println("Total ops rescheduled: " + totalOpsRescheduled);
   System.out.println("Total ops retried: " + totalOpsRetried);
  }
}
