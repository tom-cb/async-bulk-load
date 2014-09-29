async-bulk-load
===============

A sample application for performing bulk loads of data into Couchbase in an asynchronous and (mostly) none blocking way.

A quick overview of the various classes is included here:

## MainSetDriver.java
* Creates a Couchbase client connection object, setting various timeout values
* Creates a single 'value' which will later be used for all keys

#### Insert Phase
* Insert N key/value pair records into the database in a loop.
   * The inner loop does the CB async set and registers a custom MyListener class.
   * The outer loop maintains a latch and waits on the latch before moving to next iteration.
   * The insert is split into an inner and outer loop  so that if we're handling e.g. 1M items, we don't try to fire off 1M operations at a time and create a huge java heap.
   * Instead we'll issue in batches equal to 10% of the total iterations, and wait for each batch to complete before moving on to the next.


## MyListener.java
* Implements an onComplete function which is called automatically when async set has completed
* When called, onComplete checks if the operation was successful:
  * If yes, it decrements the latch.
  * If no, a new MyCallable class is created, and scheduled into a thread pool to be called backOfMillis later.

## MyCallable.java
* Implements a call() function, triggered by the thread pool after backOffMillis.
* Call function simply tries to perform async set operation again, creating and registering a new instance of MyListener.

