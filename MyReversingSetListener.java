
import com.couchbase.client.CouchbaseClient;
import java.util.concurrent.CountDownLatch;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.internal.OperationCompletionListener;


//ScheduledExecutor classes
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;





class MyReversingSetListener extends MyListener {

  MyReversingSetListener(CouchbaseClient c, CountDownLatch l, int b, Object v, String k, ScheduledExecutorService s) {
    super(c, l, b, v, k, s);
  }

  public void onComplete(OperationFuture<?> future) throws Exception {
    reverseValue();
    //System.out.println("value is:" + value);
    doSetWithBackOff(future);
  }//onComplete

  private void reverseValue() {
    String str = new StringBuilder(value.toString()).reverse().toString();
    value = str;
  }

}

