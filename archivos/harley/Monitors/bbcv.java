import Utilities.*;

class BoundedBuffer extends MyObject {  // designed for multiple producer
                                    // threads and multiple consumer threads
   private int numSlots = 0;
   private double[] buffer = null;
   private int putIn = 0, takeOut = 0;
   private int elements = 0;
   private int spaces = 0;
/*
 * The signal-and-continue effect of barging is handled here by having
 * all producer threads block if there are no spaces until woken up one
 * at a time and similarly for consumer threads and elements.  Instead of
 * the state variables spaces and elements recording the state of the
 * bounded buffer, they record the demand for spaces and elements by the
 * producer and consumer threads.  Also only notify() is necessary, not
 * notifyAll().  This is about as close as we can get to named condition
 * variables in Java: using these "notification" objects.
 */
   private Object conveyD = null, conveyF = null;

   public BoundedBuffer(int numSlots) {
      super("BoundedBuffer, numSlots="+numSlots);
      if (numSlots <= 0) throw new IllegalArgumentException("numSlots<=0");
      this.numSlots = numSlots;
      buffer = new double[numSlots];
      for (int i = 0; i < numSlots; i++) buffer[i] = 0.0;
      elements = 0;
      spaces = numSlots;
      conveyD = new Object();
      conveyF = new Object();
      System.out.println(this + " is alive");
   }

   public void deposit(double value) {
      synchronized (conveyD) { // grab a space or wait behind others
         spaces--;
         if (spaces < 0) {
            while (true) {
               try {
                  conveyD.wait();
                  break;                           // notified so ok
               } catch (InterruptedException e) {
                  // handle notify() after interrupt() race condition
                  if (spaces >= 0) break; // notify() after interrupt()
                  else continue;      // interrupted so continue waiting
               }
            }
         }
         buffer[putIn] = value;
         putIn = (putIn + 1) % numSlots;
      }
      synchronized (conveyF) { // signal a waiting consumer if there is one
         elements++;
         if (elements <= 0) conveyF.notify();
      }
   }

   public double fetch() {
      double value;
      synchronized (conveyF) { // grab an element or wait behind others
         elements--;
         if (elements < 0) {
            while (true) {
               try {
                  conveyF.wait();
                  break;                            // notified so ok
               } catch (InterruptedException e) {
                  // handle notify() after interrupt() race condition
                  if (elements >= 0) break; // notify() after interrupt()
                  else continue;      // interrupted so continue waiting
               }
            }
         }
         value = buffer[takeOut];
         takeOut = (takeOut + 1) % numSlots;
      }
      synchronized (conveyD) { // signal a waiting producer if there is one
         spaces++;
         if (spaces <= 0) conveyD.notify();
      }
      return value;
   }

   public String toString() { // should be synchronized on conveyD,R
      return getName() + ", spaces=" + spaces + ", elements=" + elements;
   }
}

/* ............... Example compile and run(s)

D:\>javac bbcv.java bbml.java

D:\>java ProducersConsumers -R10
ProducersConsumers:
 numSlots=20, numProducers=2, numConsumers=3, pNap=3, cNap=2, runTime=10
BoundedBuffer, numSlots=20, spaces=20, elements=0 is alive
age=50, PRODUCER0 napping for 2594 ms
age=50, PRODUCER1 napping for 1774 ms
All threads started
age=110, Consumer1 napping for 548 ms
age=110, Consumer0 napping for 1740 ms
age=110, Consumer2 napping for 898 ms
age=660, Consumer1 wants to consume
age=1040, Consumer2 wants to consume
age=1870, PRODUCER1 produced item 0.86839
age=1870, PRODUCER1 deposited item 0.86839
age=1870, PRODUCER1 napping for 989 ms
age=1870, Consumer0 wants to consume
age=1870, Consumer1 fetched item 0.86839
age=1870, Consumer1 napping for 1711 ms
age=2690, PRODUCER0 produced item 0.507812
age=2690, PRODUCER0 deposited item 0.507812
age=2690, PRODUCER0 napping for 1008 ms
age=2690, Consumer2 fetched item 0.507812
age=2690, Consumer2 napping for 1038 ms
age=2850, PRODUCER1 produced item 0.768203
age=2850, PRODUCER1 deposited item 0.768203
age=2850, PRODUCER1 napping for 1366 ms
age=2850, Consumer0 fetched item 0.768203
age=2850, Consumer0 napping for 1460 ms
age=3620, Consumer1 wants to consume
age=3680, PRODUCER0 produced item 0.0275597
age=3680, PRODUCER0 deposited item 0.0275597
age=3680, PRODUCER0 napping for 59 ms
age=3680, Consumer1 fetched item 0.0275597
age=3680, Consumer1 napping for 741 ms
age=3730, Consumer2 wants to consume
age=3730, PRODUCER0 produced item 0.65506
age=3730, PRODUCER0 deposited item 0.65506
age=3730, PRODUCER0 napping for 456 ms
age=3730, Consumer2 fetched item 0.65506
age=3730, Consumer2 napping for 1899 ms
age=4230, PRODUCER0 produced item 0.201681
age=4230, PRODUCER0 deposited item 0.201681
age=4230, PRODUCER0 napping for 2697 ms
age=4230, PRODUCER1 produced item 0.739107
age=4230, PRODUCER1 deposited item 0.739107
age=4230, PRODUCER1 napping for 748 ms
age=4340, Consumer0 wants to consume
age=4340, Consumer0 fetched item 0.201681
age=4340, Consumer0 napping for 385 ms
age=4450, Consumer1 wants to consume
age=4450, Consumer1 fetched item 0.739107
age=4450, Consumer1 napping for 1293 ms
age=4720, Consumer0 wants to consume
age=5000, PRODUCER1 produced item 0.437976
age=5000, PRODUCER1 deposited item 0.437976
age=5000, PRODUCER1 napping for 2489 ms
age=5000, Consumer0 fetched item 0.437976
age=5000, Consumer0 napping for 714 ms
age=5650, Consumer2 wants to consume
age=5710, Consumer0 wants to consume
age=5760, Consumer1 wants to consume
age=6920, PRODUCER0 produced item 0.444391
age=6920, PRODUCER0 deposited item 0.444391
age=6920, PRODUCER0 napping for 2621 ms
age=6920, Consumer2 fetched item 0.444391
age=6920, Consumer2 napping for 1248 ms
age=7470, PRODUCER1 produced item 0.92733
age=7470, PRODUCER1 deposited item 0.92733
age=7470, PRODUCER1 napping for 1145 ms
age=7470, Consumer0 fetched item 0.92733
age=7470, Consumer0 napping for 205 ms
age=7690, Consumer0 wants to consume
age=8180, Consumer2 wants to consume
age=8620, PRODUCER1 produced item 0.223514
age=8620, PRODUCER1 deposited item 0.223514
age=8620, PRODUCER1 napping for 933 ms
age=8620, Consumer1 fetched item 0.223514
age=8620, Consumer1 napping for 711 ms
age=9330, Consumer1 wants to consume
age=9550, PRODUCER0 produced item 0.946951
age=9550, PRODUCER0 deposited item 0.946951
age=9550, PRODUCER0 napping for 2683 ms
age=9550, Consumer0 fetched item 0.946951
age=9550, Consumer0 napping for 466 ms
age=9550, PRODUCER1 produced item 0.49459
age=9550, PRODUCER1 deposited item 0.49459
age=9550, PRODUCER1 napping for 351 ms
age=9550, Consumer2 fetched item 0.49459
age=9550, Consumer2 napping for 986 ms
age=9940, PRODUCER1 produced item 0.444163
age=9940, PRODUCER1 deposited item 0.444163
age=9940, PRODUCER1 napping for 2310 ms
age=9940, Consumer1 fetched item 0.444163
age=9940, Consumer1 napping for 643 ms
age=10050, Consumer0 wants to consume
age()=10100, time to stop the threads and exit
                                            ... end of example run(s)  */
