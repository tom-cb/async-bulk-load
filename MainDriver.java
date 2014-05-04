import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;
import com.couchbase.client.CouchbaseConnectionFactory;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.internal.OperationCompletionListener;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.GetCompletionListener;
import java.util.Random;

//ScheduledExecutor classes
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class MainDriver {

  public static void main(String[] args) throws Exception {

    //Thread pool will be used to re-issue failed ops
    //Also used to issue further work based on results of ops
    ScheduledExecutorService schExSvc = Executors.newScheduledThreadPool(8);

    // ******* Client Object Setup and Connection **********
    // *****************************************************

    // (Subset) of nodes in the cluster to establish a connection
    List<URI> hosts = Arrays.asList(
      new URI("http://188.226.229.190:8091/pools")
    );
 
    // Name of the Bucket to connect to
    String bucket = "default";
 
    // Password of the bucket (empty) string if none
    String password = "";

    CouchbaseConnectionFactoryBuilder cfb = new CouchbaseConnectionFactoryBuilder();

    // Ovveride default values on CouchbaseConnectionFactoryBuilder
    // For example - wait up to 10 seconds for an operation to succeed
    cfb.setOpTimeout(1000);
    cfb.setOpQueueMaxBlockTime(30000);

    CouchbaseConnectionFactory cf = cfb.buildCouchbaseConnection(hosts, "default", "");

    CouchbaseClient client = new CouchbaseClient(cf); 

    // ******* Create some data to use as our value ********
    // *****************************************************

    // Create a string to use as our 'value' for storing
    // Loop adding multiples of that to give larger value for testing
    // Creating 2500 char string here by default
    String value = "";
    for (int i = 0; i < 250; i++) {
      value += "0123456789";
    }


    System.out.println("Length of value string: " + value.getBytes("UTF-8").length);

    // Set the number of key/value pairs to create
    int iterations = 10000000; 

    OpTracker opTracker = new OpTracker();


    // ******* Create and issue async sets initial data ****
    // *****************************************************

    long phaseOneStartTime = System.currentTimeMillis();

    for (int s=0; s<10; s++)
    {
	  final CountDownLatch latch = new CountDownLatch(iterations/10);

	  for (int i = 0; i < (iterations/10); i++) {
          String key = "key-" + s + "-" + i;
          try {
  	        OperationFuture<Boolean> future = client.set(key, value);
            opTracker.setScheduled(key);
	   	    future.addListener(new MyListener(client,latch, 0, value, key, schExSvc, opTracker));
          }
          catch (Exception e) {
            System.out.println("exception: " + e.getMessage());
            throw e;
          }
	  }
	  latch.await();
          //We've finished that batch, so hint to system now is good time for gc before next batch
          System.gc();
      //System.out.println("final latch value: " + latch.getCount());
    }

    long phaseOneEndTime = System.currentTimeMillis();

    System.out.println("completed " + iterations + " *SET* opeerations in " + ((phaseOneEndTime - phaseOneStartTime)/1000) + " seconds" );
    //opTracker.printTracker();


    // *** Retrieve each document, reverse it, and store back ***
    // *****************************************************

    long phaseTwoStartTime = System.currentTimeMillis();
    for (int s=0; s<10; s++)
    {
        //doubling iterations to allow for get and set
	  final CountDownLatch latch = new CountDownLatch(iterations/10);
	  final CountDownLatch setLatch = new CountDownLatch(iterations/10);

	  for (int i = 0; i < (iterations/10); i++) {
          String key = "key-" + s + "-" + i;
          try {
  	        GetFuture<java.lang.Object> future = client.asyncGet(key.toString());
            opTracker.setScheduled(key);
  	   	    future.addListener(new MyReversingListener(client,latch, 0, value, key, schExSvc, setLatch, opTracker));
          }
          catch (Exception e) {
            System.out.println("exception: " + e.getMessage());
            throw e;
          }
	  }
      latch.await();
      setLatch.await();

      //We've finished that batch, so hint to system now is good time for gc before next batch
      System.gc();
    }

    long phaseTwoEndTime = System.currentTimeMillis();

    System.out.println("completed " + iterations + " *GET + SET*  operations in " + ((phaseTwoEndTime - phaseTwoStartTime)/1000) + " seconds" );


    //System.out.println("Op tracker results: " );
    //opTracker.printTracker();

    // Shutting down properly
    client.shutdown();
    // Shutdown ScheduledExecutor
    schExSvc.shutdown(); 
  }
}
