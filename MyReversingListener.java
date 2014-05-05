
import com.couchbase.client.CouchbaseClient;
import java.util.concurrent.CountDownLatch;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.GetCompletionListener;

import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.internal.OperationCompletionListener;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


    class MyReversingListener extends MyGetListener {
      CountDownLatch setLatch;

      MyReversingListener(CouchbaseClient c, CountDownLatch l, int b, Object v, String k,
          ScheduledExecutorService s, CountDownLatch sLatch, OpTracker ot) {
        super(c, l, b, v, k, s, ot);
        setLatch = sLatch;
      }

      public void doPostProcess(GetFuture<?> future) throws Exception {
        try {
          value = future.get();
        }
        catch (Exception e) {
          System.out.println("exception: " + e.getMessage());
          System.exit(1);
        }

        try {
          value = new StringBuilder(value.toString()).reverse().toString();
        }
        catch (Exception e) {
          System.out.println("exception reversing string: " + e.getMessage());
          System.exit(1);
        }

        try {
    	  OperationFuture<Boolean> set_future = client.set(key, value);
          opTracker.setScheduled(key);
    	  set_future.addListener(new MyListener(client, setLatch, 0, value, key, sch, opTracker));
        }
        catch (Exception e) {
          System.out.println("exception: " + e.getMessage());
          System.exit(1);
        }
      } 

      public Callable getCallableForRetry() {
        return new MyReversingCallable(this, opTracker);  
      }
    } //MyReversingListener
