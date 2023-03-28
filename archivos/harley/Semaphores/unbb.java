import Utilities.*;
import Synchronization.*;

class CorruptedQueueException extends Exception {}

class QueueItem extends MyObject {

   protected double value = 0;
   protected QueueItem nextItem = null;

   protected QueueItem() { super(); }
   protected QueueItem(double value) { this.value = value; }
}

class UnboundedBuffer extends QueueItem { // can be used with multiple
                                          // producers and consumers
// extend QueueItem so that only this class can access value and nextItem
   private int count = 0;
   private CountingSemaphore elements = new CountingSemaphore(0);
   private BinarySemaphore mutex = new BinarySemaphore(1);
   private QueueItem head = null;
   private QueueItem tail = null;

   public UnboundedBuffer() {
      System.out.println("UnboundedBuffer alive");
   }

   public void deposit(double value) throws CorruptedQueueException {
      P(mutex);
      QueueItem oldTail = null;
      QueueItem newOne = new QueueItem(value);
      if (tail == null) { // add to an empty queue
         if (head != null) throw new CorruptedQueueException();
         else {
            head = newOne; tail = newOne;
         }
      } else {            // add to end of queue
         oldTail = tail; tail = newOne;
         oldTail.nextItem = newOne;
      }
      count++;
      System.out.println("deposit: count=" + count);
      V(mutex);
      V(elements);
   }

   public double fetch() throws CorruptedQueueException {
      double value;
      QueueItem oldOne = null;
      P(elements);
      P(mutex);
      if (head == null && tail == null)        // elements semaphore
         throw new CorruptedQueueException();  // should prevent this
      else if (head != null && tail != null) {
         if (head == tail) { // dequeue from a singleton queue
            oldOne = head;
            head = null; tail = null;
         } else {            // dequeue from beginning of queue
            oldOne = head;
            head = oldOne.nextItem;
         }
         count--;
         System.out.println("fetch: count=" + count);
      } else throw new CorruptedQueueException();
      value = oldOne.value;
      V(mutex);
      return value;
   }
}

/* ............... Example compile and run(s)

D:\>javac unbb.java bbml.java

D:\>java ProducersConsumers -P3 -C2 -c2 -p2 -R3
ProducersConsumers: numProducers=3, numConsumers=2, pNap=2, cNap=2, runTime=3
UnboundedBuffer alive
All threads started
age=110, PRODUCER 0 napping for 688 ms
age=160, Consumer 0 napping for 966 ms
age=110, PRODUCER 1 napping for 1523 ms
age=160, PRODUCER 2 napping for 832 ms
age=160, Consumer 1 napping for 1488 ms
age=880, PRODUCER 0 produced item 0.434521
deposit: count=1
PRODUCER 0 deposited item 0.434521
age=880, PRODUCER 0 napping for 1831 ms
age=1040, PRODUCER 2 produced item 0.696984
deposit: count=2
PRODUCER 2 deposited item 0.696984
age=1040, PRODUCER 2 napping for 143 ms
age=1150, Consumer 0 wants to consume
fetch: count=1
Consumer 0 fetched item 0.434521
age=1150, Consumer 0 napping for 1980 ms
age=1150, PRODUCER 2 produced item 0.0704563
deposit: count=2
PRODUCER 2 deposited item 0.0704563
age=1150, PRODUCER 2 napping for 1395 ms
age=1700, Consumer 1 wants to consume
fetch: count=1
Consumer 1 fetched item 0.696984
age=1700, Consumer 1 napping for 52 ms
age=1700, PRODUCER 1 produced item 0.233638
deposit: count=2
PRODUCER 1 deposited item 0.233638
age=1700, PRODUCER 1 napping for 1745 ms
age=1760, Consumer 1 wants to consume
fetch: count=1
Consumer 1 fetched item 0.0704563
age=1760, Consumer 1 napping for 1379 ms
age=2580, PRODUCER 2 produced item 0.978749
deposit: count=2
PRODUCER 2 deposited item 0.978749
age=2580, PRODUCER 2 napping for 856 ms
age=2690, PRODUCER 0 produced item 0.692208
deposit: count=3
PRODUCER 0 deposited item 0.692208
age=2690, PRODUCER 0 napping for 1812 ms
age=3130, Consumer 0 wants to consume
fetch: count=2
Consumer 0 fetched item 0.233638
age=3130, Consumer 0 napping for 1269 ms
age()=3130, time to stop the threads and exit

D:\>java ProducersConsumers -P2 -C3 -c2 -p2 -R3
ProducersConsumers: numProducers=2, numConsumers=3, pNap=2, cNap=2, runTime=3
UnboundedBuffer alive
All threads started
age=110, PRODUCER 0 napping for 667 ms
age=110, PRODUCER 1 napping for 1117 ms
age=110, Consumer 0 napping for 816 ms
age=110, Consumer 1 napping for 521 ms
age=110, Consumer 2 napping for 798 ms
age=660, Consumer 1 wants to consume
age=770, PRODUCER 0 produced item 0.934868
deposit: count=1
PRODUCER 0 deposited item 0.934868
age=770, PRODUCER 0 napping for 1479 ms
fetch: count=0
Consumer 1 fetched item 0.934868
age=770, Consumer 1 napping for 1291 ms
age=930, Consumer 2 wants to consume
age=930, Consumer 0 wants to consume
age=1260, PRODUCER 1 produced item 0.420986
deposit: count=1
PRODUCER 1 deposited item 0.420986
age=1260, PRODUCER 1 napping for 537 ms
fetch: count=0
Consumer 2 fetched item 0.420986
age=1260, Consumer 2 napping for 1386 ms
age=1810, PRODUCER 1 produced item 0.426525
deposit: count=1
PRODUCER 1 deposited item 0.426525
age=1810, PRODUCER 1 napping for 44 ms
fetch: count=0
Consumer 0 fetched item 0.426525
age=1810, Consumer 0 napping for 1598 ms
age=1860, PRODUCER 1 produced item 0.923759
deposit: count=1
PRODUCER 1 deposited item 0.923759
age=1860, PRODUCER 1 napping for 1787 ms
age=2080, Consumer 1 wants to consume
fetch: count=0
Consumer 1 fetched item 0.923759
age=2080, Consumer 1 napping for 895 ms
age=2250, PRODUCER 0 produced item 0.650406
deposit: count=1
PRODUCER 0 deposited item 0.650406
age=2250, PRODUCER 0 napping for 1478 ms
age=2630, Consumer 2 wants to consume
fetch: count=0
Consumer 2 fetched item 0.650406
age=2630, Consumer 2 napping for 48 ms
age=2690, Consumer 2 wants to consume
age=2960, Consumer 1 wants to consume
age()=3070, time to stop the threads and exit
                                            ... end of example run(s)  */
