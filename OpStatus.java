

public class OpStatus {

  //public as im too lazy to write getters and setters
  public int scheduled = 0;
  public int rescheduled = 0;
  public int retried = 0;
  public int completed = 0;
  public int onCompleteCalled = 0;

  public void setScheduled() {
    scheduled++;
  }

  public void setRescheduled() {
    rescheduled++;
  }

  public void setRetried() {
    retried++;
  }

  public void setCompleted() {
    completed++;
  }

  public void setOnCompleted() {
    onCompleteCalled++;
  }

  public String toString() {

    if (completed != scheduled) {
      return "*** Did not complete op expected number of times: Complted= " + completed + " scheduled=" + scheduled + ".";
    }
    // Only bother outputting stats for ops that got retried
    if (rescheduled > 0) {
      return "Completed: " + completed + " Scheduled: " + scheduled + 
        " Rescheduled: " + rescheduled + " onCompleted: " + onCompleteCalled + ".";
    }
    else { return ""; }

  }
}
