
import com.couchbase.client.CouchbaseClient;
import java.util.concurrent.CountDownLatch;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.GetCompletionListener;


//ScheduledExecutor classes
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;



class MyReversingCallable implements Callable {
  MyReversingListener listener;
  OpTracker opTracker;

  MyReversingCallable(MyReversingListener l, OpTracker ot) {
    listener = l; 
    opTracker = ot;
  }

  public Object call() throws Exception {
    try {
      // Perform the async call that forms the retry
      GetFuture<java.lang.Object> next_future = listener.client.asyncGet(listener.key);
      opTracker.setRetried(listener.key);
      next_future.addListener(new MyReversingListener(listener.client, listener.latch, listener.backoffexp,
         listener.value, listener.key, listener.sch, listener.setLatch, listener.opTracker));
    }
   	catch (Exception e) { 
      System.out.println("caught exception: " + e.getMessage()); 
      System.exit(1); 
    }

    return "no idea what this string is for";
  }

}//MyGetCallable


