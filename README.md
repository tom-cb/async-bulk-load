async-bulk-load
===============


A sample application for performing bulk loads of data into Couchbase in an asynchronous / (mostly) none blocking way.

A quick overview of the various classes is included here:

## MainDriver.java
* Create Couchbase client connection object, setting various timeout values
* Create a single 'value' which will later be used for all keys

#### Insert Phase
* Insert N key/value pair records into the database in a loop.
   * The inner loop does the CB set and registers a customer MyListener class.
   * The outer loop maintains a latch and waits on the latch before moving to next iteration.
   * The insert is split into an inner and outer loop  so that if we're handling e.g. 10M items, we don't try to fire off 10M operations at a time and create a huge java heap. Instead we'll issue in batch equal to 10% of the total iterations, and wait for each batch to complete before moving on to the next.


#### Read, Modify, Write Phase
* Once N items have been inserted into the database above, the main driver goes into a second phase.
* Each value is retrieved from the database, reversed, and stored back to the db again.
* Two additional custom listeners are used here, MyGetListener and MyReversingListener (which extends the former).


## MyListener.java
* Implements an onComplete function for set operations.
* When called, onComplete checks if the operation was successful:
  * If yes, it decrements the latch.
  * If no, a new MyCallable class is created, and scheduled into a thread pool to be called backOfMillis later.

## MyCallable.java
* Implements a call() function, triggered by the thread pool after backOffMillis.
* Call function simply tries to perform set operation again, creating and registering a new instance of MyListener.

## MyGetListener.java
* Implements an onComplete function similar to MyListener, but for gets.
* Main difference is that once data is successfully retrieved a doPostProcess function is called.
* Default implementation of doPostProcess is a nop

## MyReversingListener.java
* Extends MyGetListener, with an over-riding implementation of doPostProcess.
* Post processing takes the retrieved value, and issues a set back to the db, creating a MyListener as previously to check results and issue retries.

## OpTracker.java and OpStatus.java
* Used to log info about how many operations have been scheduled for each key, how many retries were issued, and whether each operation has completed successfully.
* All setup already, to view the output just comment out the setLatch.await() and swap for the setLatch.await(5, TimeUnit) line, then uncomment the opTracker.print() line 
