import com.couchbase.client.CouchbaseClient;
import java.util.concurrent.CountDownLatch;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.GetCompletionListener;


import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


    class MyGetListener implements GetCompletionListener {
      ScheduledExecutorService sch;
	  Object value;
      String key;
      CountDownLatch latch;
      CouchbaseClient client;
      int backoffexp = 0;
      int tries = 100;
      OpTracker opTracker;

      MyGetListener(CouchbaseClient c, CountDownLatch l, int b, Object v, String k, ScheduledExecutorService s, OpTracker ot) {
        value = v;
        key = k;
        client = c;
        latch = l;
        backoffexp = b;
        sch = s;
        opTracker = ot;
      }

      public void onComplete(GetFuture<?> future) throws Exception {
        opTracker.setOnCompleted(key);
        doGetWithBackOff(future);
      }

      public void doPostProcess(GetFuture<?> future) throws Exception {
            value = future.get();
      }

      public void doGetWithBackOff(GetFuture<?> future) throws Exception {
      try {
        if (future.getStatus().isSuccess()) {
          try {
            opTracker.setCompleted(key);
      	    latch.countDown();
          }
          catch (Exception e) { System.out.println("couldnt countdown latch: " + e.getMessage()); System.exit(1); }

          // do any additional work
          doPostProcess(future);
        }
        else if (backoffexp > tries) {
          System.out.println("tried " + tries + " times, giving up GETing key: " + key);
          System.exit(1);
        }
        else {
          // The operation failed, reschedule it for backoffMillis time later
          //System.out.println("op failed! : " + future.getKey() + " : " + future.getStatus().getMessage());
          try {
            double backoffMillis = Math.pow(2, backoffexp);
            backoffMillis = Math.min(1000, backoffMillis); // 1 sec max

            backoffexp++;
            
            if (sch == null) { System.out.println("no scheduler object!"); System.exit(1); }

            opTracker.setRescheduled(key);

            ScheduledFuture scheduledFuture =
              sch.schedule(getCallableForRetry(), (long)backoffMillis, TimeUnit.MILLISECONDS);
	  } 
          catch (Exception e) { 
            System.out.println("borked during back off");
            System.exit(1);
          }
        }
       }//try
       catch(Exception e) {
         System.out.println("exception in onComplete");
         System.exit(1);
       }
      }//doGetWithBackOff

      public Callable getCallableForRetry() {
        return new MyGetCallable(this, opTracker);  
      }

    } //MyGetListener
