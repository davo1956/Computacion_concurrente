import Utilities.*;
import Synchronization.*;

class BoundedBuffer extends MyObject { // designed for a single
                      // producer thread and a single consumer thread
   private int numSlots = 0;
   private double[] buffer = null;
   private int putIn = 0, takeOut = 0;
   private int count = 0;
   private BinarySemaphore mutex = null;
   private CountingSemaphore elements = null;
   private CountingSemaphore spaces = null;

   public BoundedBuffer(int numSlots) {
      super("BoundedBuffer with " + numSlots + " slots");
      if (numSlots <= 0) throw new IllegalArgumentException("numSlots<=0");
      this.numSlots = numSlots;
      buffer = new double[numSlots];
      mutex = new BinarySemaphore(1);
      elements = new CountingSemaphore(0);
      spaces = new CountingSemaphore(numSlots);
      System.out.println("BoundedBuffer alive, numSlots=" + numSlots);
   }

   public void deposit(double value) {
      P(spaces);
      buffer[putIn] = value;
      putIn = (putIn + 1) % numSlots;
      P(mutex);
      count++;
      System.out.println(" after deposit, count=" + count
         + ", putIn=" + putIn);
      V(mutex);
      V(elements);
   }

   public double fetch() {
      double value;
      P(elements);
      value = buffer[takeOut];
      takeOut = (takeOut + 1) % numSlots;
      P(mutex);
      count--;
      System.out.println(" after fetch, count=" + count
         + ", takeOut=" + takeOut);
      V(mutex);
      V(spaces);
      return value;
   }
}

/* ............... Example compile and run(s)

D:\>javac bbou.java bbpc.java

D:\>java ProducerConsumer -s 4 -p 1 -c 4 -R 10
ProducerConsumer: numSlots=4, pNap=1, cNap=4, runTime=10
BoundedBuffer alive, numSlots=4
All threads started
age=110, Consumer napping for 3589 ms
age=110, PRODUCER napping for 363 ms
age=500, PRODUCER produced item 0.819345
 after deposit, count=1, putIn=1
PRODUCER deposited item 0.819345
age=500, PRODUCER napping for 402 ms
age=940, PRODUCER produced item 0.433438
 after deposit, count=2, putIn=2
PRODUCER deposited item 0.433438
age=940, PRODUCER napping for 444 ms
age=1380, PRODUCER produced item 0.72863
 after deposit, count=3, putIn=3
PRODUCER deposited item 0.72863
age=1380, PRODUCER napping for 357 ms
age=1760, PRODUCER produced item 0.338516
 after deposit, count=4, putIn=0
PRODUCER deposited item 0.338516
age=1760, PRODUCER napping for 294 ms
age=2040, PRODUCER produced item 0.685693
age=3740, Consumer wants to consume
 after fetch, count=3, takeOut=1
Consumer fetched item 0.819345
age=3740, Consumer napping for 1699 ms
 after deposit, count=4, putIn=1
PRODUCER deposited item 0.685693
age=3740, PRODUCER napping for 140 ms
age=3900, PRODUCER produced item 0.673757
age=5440, Consumer wants to consume
 after fetch, count=3, takeOut=2
Consumer fetched item 0.433438
age=5440, Consumer napping for 329 ms
 after deposit, count=4, putIn=2
PRODUCER deposited item 0.673757
age=5440, PRODUCER napping for 845 ms
age=5770, Consumer wants to consume
 after fetch, count=3, takeOut=3
Consumer fetched item 0.72863
age=5770, Consumer napping for 2362 ms
age=6270, PRODUCER produced item 0.743639
 after deposit, count=4, putIn=3
PRODUCER deposited item 0.743639
age=6270, PRODUCER napping for 219 ms
age=6480, PRODUCER produced item 0.741701
age=8130, Consumer wants to consume
 after fetch, count=3, takeOut=0
Consumer fetched item 0.338516
age=8130, Consumer napping for 2042 ms
 after deposit, count=4, putIn=0
PRODUCER deposited item 0.741701
age=8130, PRODUCER napping for 374 ms
age=8520, PRODUCER produced item 0.0208527
age()=10060, time to stop the threads and exit

D:\>java ProducerConsumer -s 4 -p 4 -c 1 -R 10
ProducerConsumer: numSlots=4, pNap=4, cNap=1, runTime=10
BoundedBuffer alive, numSlots=4
All threads started
age=170, PRODUCER napping for 640 ms
age=170, Consumer napping for 334 ms
age=490, Consumer wants to consume
age=820, PRODUCER produced item 0.573534
 after deposit, count=1, putIn=1
PRODUCER deposited item 0.573534
age=820, PRODUCER napping for 930 ms
 after fetch, count=0, takeOut=1
Consumer fetched item 0.573534
age=820, Consumer napping for 654 ms
age=1480, Consumer wants to consume
age=1760, PRODUCER produced item 0.370371
 after deposit, count=1, putIn=2
PRODUCER deposited item 0.370371
age=1760, PRODUCER napping for 2340 ms
 after fetch, count=0, takeOut=2
Consumer fetched item 0.370371
age=1760, Consumer napping for 303 ms
age=2030, Consumer wants to consume
age=4060, PRODUCER produced item 0.899359
 after deposit, count=1, putIn=3
PRODUCER deposited item 0.899359
age=4060, PRODUCER napping for 3270 ms
 after fetch, count=0, takeOut=3
Consumer fetched item 0.899359
age=4060, Consumer napping for 329 ms
age=4450, Consumer wants to consume
age=7360, PRODUCER produced item 0.74957
 after deposit, count=1, putIn=0
PRODUCER deposited item 0.74957
age=7360, PRODUCER napping for 1451 ms
 after fetch, count=0, takeOut=0
Consumer fetched item 0.74957
age=7360, Consumer napping for 647 ms
age=8020, Consumer wants to consume
age=8840, PRODUCER produced item 0.520785
 after deposit, count=1, putIn=1
PRODUCER deposited item 0.520785
age=8840, PRODUCER napping for 809 ms
 after fetch, count=0, takeOut=1
Consumer fetched item 0.520785
age=8840, Consumer napping for 38 ms
age=8900, Consumer wants to consume
age=9610, PRODUCER produced item 0.863465
 after deposit, count=1, putIn=2
PRODUCER deposited item 0.863465
age=9610, PRODUCER napping for 2653 ms
 after fetch, count=0, takeOut=2
Consumer fetched item 0.863465
age=9610, Consumer napping for 714 ms
age()=10110, time to stop the threads and exit
                                            ... end of example run(s)  */
