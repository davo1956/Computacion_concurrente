import Synchronization.*;
import Utilities.*;

class ConsumerCondition extends MyObject implements Condition {

   private double value = 0;

   public ConsumerCondition(double value) {
      super("ConsumerCondition: value=" + value);
      this.value = value;
   }

   public boolean checkCondition(Object message) {
      return ((Double) message).doubleValue() < value;
   }
}

class Producer extends MyObject implements Runnable {

   private int pNap = 0; // milliseconds
   private ConditionalMessagePassing cmp = null;

   public Producer(String name, int pNap, ConditionalMessagePassing cmp) {
      super(name);
      this.pNap = pNap;
      this.cmp = cmp;
      new Thread(this).start();
   }

   public void run() {
      double item;
      int napping;
      while (true) {
         napping = 1 + (int) random(pNap);
         System.out.println("age=" + age() + ", " + getName()
            + " napping for " + napping + " ms");
         nap(napping);
         item = random();
         System.out.println("age=" + age() + ", " + getName()
            + " produced item " + item);
         cmp.send(new Double(item));
         System.out.println("age=" + age() + ", " + getName()
            + " deposited item " + item);
      }
   }
}

class Consumer extends MyObject implements Runnable {

   private int cNap = 0; // milliseconds
   private ConditionalMessagePassing cmp = null;

   public Consumer(String name, int cNap, ConditionalMessagePassing cmp) {
      super(name);
      this.cNap = cNap;
      this.cmp = cmp;
      new Thread(this).start();
   }

   public void run() {
      double item;
      int napping;
      while (true) {
         napping = 1 + (int) random(cNap);
         System.out.println("age=" + age() + ", " + getName()
            + " napping for " + napping + " ms");
         nap(napping);
         double limit = random();
         System.out.println("age=" + age() + ", " + getName()
            + " wants to consume item less than " + limit);
         item =
          ((Double) cmp.receive(new ConsumerCondition(limit))).doubleValue();
         System.out.println("age=" + age() + ", " + getName()
            + " fetched item " + item);
      }
   }
}

class ProducersConsumers extends MyObject {

   public static void main(String[] args) {

      // parse command line arguments, if any, to override defaults
      GetOpt go = new GetOpt(args, "UASP:C:p:c:R:");
      go.optErr = true;
      String usage =
         "Usage: -A|-S -P numP -C numC -p pNap -c cNap -R runTime";
      int ch = -1;
      boolean synchronous = false;
      int numProducers = 2;
      int numConsumers = 3;
      int pNap = 2;       // defaults
      int cNap = 3;       // in
      int runTime = 60;   // seconds
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'A') synchronous = false;
         else if ((char)ch == 'S') synchronous = true;
         else if ((char)ch == 'P')
            numProducers = go.processArg(go.optArgGet(), numProducers);
         else if ((char)ch == 'C')
            numConsumers = go.processArg(go.optArgGet(), numConsumers);
         else if ((char)ch == 'p')
            pNap = go.processArg(go.optArgGet(), pNap);
         else if ((char)ch == 'c')
            cNap = go.processArg(go.optArgGet(), cNap);
         else if ((char)ch == 'R')
            runTime = go.processArg(go.optArgGet(), runTime);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("ProducersConsumers:\n synchronous=" + synchronous
         + ", numProducers=" + numProducers
         + ", numConsumers=" + numConsumers
         + ", pNap=" + pNap + ", cNap=" + cNap + ", runTime=" + runTime);

      // create the conditional message passing channel
         ConditionalMessagePassing cmp = null;
         if (synchronous) cmp = new SyncConditionalMessagePassing(true);
         else             cmp = new AsyncConditionalMessagePassing(false);

      // start the Producer and Consumer threads
      for (int i = 0; i < numProducers; i++)
         new Producer("PRODUCER"+i, pNap*1000, cmp);
      for (int i = 0; i < numConsumers; i++)
         new Consumer("Consumer"+i, cNap*1000, cmp);
      System.out.println("All threads started");

      // let them run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the threads and exit");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac mlpc.java

D:\>java ProducersConsumers -S -R7
ProducersConsumers:
 synchronous=true, numProducers=2, numConsumers=3, pNap=2, cNap=3, runTime=7
SCMP: matching 0 messages against 0 receivers
SCMP: waiting
All threads started
age=110, PRODUCER0 napping for 1761 ms
age=110, PRODUCER1 napping for 302 ms
age=110, Consumer0 napping for 2644 ms
age=110, Consumer1 napping for 2759 ms
age=160, Consumer2 napping for 2481 ms
age=440, PRODUCER1 produced item 0.304279
SCMP: send notify
SCMP: done waiting
SCMP: matching 1 messages against 0 receivers
SCMP: waiting
age=1920, PRODUCER0 produced item 0.502942
SCMP: send notify
SCMP: done waiting
SCMP: matching 2 messages against 0 receivers
SCMP: waiting
age=2630, Consumer2 wants to consume item less than 0.899137
SCMP: receive notify
SCMP: done waiting
SCMP: matching 2 messages against 1 receivers
age=2630, Consumer2 fetched item 0.304279
age=2630, Consumer2 napping for 1991 ms
SCMP: matched 0 with 0
SCMP: matching 1 messages against 0 receivers
SCMP: waiting
age=2630, PRODUCER1 deposited item 0.304279
age=2690, PRODUCER1 napping for 1493 ms
age=2800, Consumer0 wants to consume item less than 0.651279
SCMP: receive notify
SCMP: done waiting
SCMP: matching 1 messages against 1 receivers
age=2800, Consumer0 fetched item 0.502942
age=2800, Consumer0 napping for 30 ms
SCMP: matched 0 with 0
SCMP: matching 0 messages against 0 receivers
SCMP: waiting
age=2800, PRODUCER0 deposited item 0.502942
age=2800, PRODUCER0 napping for 1218 ms
age=2850, Consumer0 wants to consume item less than 0.474079
SCMP: receive notify
SCMP: done waiting
SCMP: matching 0 messages against 1 receivers
SCMP: waiting
age=2910, Consumer1 wants to consume item less than 0.822192
SCMP: receive notify
SCMP: done waiting
SCMP: matching 0 messages against 2 receivers
SCMP: waiting
age=4010, PRODUCER0 produced item 0.940281
SCMP: send notify
SCMP: done waiting
SCMP: matching 1 messages against 2 receivers
SCMP: no matches found
SCMP: waiting
age=4170, PRODUCER1 produced item 0.419674
SCMP: send notify
SCMP: done waiting
SCMP: matching 2 messages against 2 receivers
age=4170, Consumer0 fetched item 0.419674
age=4170, Consumer0 napping for 2505 ms
SCMP: matched 1 with 0
SCMP: matching 1 messages against 1 receivers
SCMP: no matches found
SCMP: waiting
age=4170, PRODUCER1 deposited item 0.419674
age=4170, PRODUCER1 napping for 1411 ms
age=4670, Consumer2 wants to consume item less than 0.237627
SCMP: receive notify
SCMP: done waiting
SCMP: matching 1 messages against 2 receivers
SCMP: no matches found
SCMP: waiting
age=5600, PRODUCER1 produced item 0.0163549
SCMP: send notify
SCMP: done waiting
SCMP: matching 2 messages against 2 receivers
age=5600, Consumer1 fetched item 0.0163549
age=5600, Consumer1 napping for 973 ms
SCMP: matched 1 with 0
SCMP: matching 1 messages against 1 receivers
age=5600, PRODUCER1 deposited item 0.0163549
age=5600, PRODUCER1 napping for 865 ms
SCMP: no matches found
SCMP: waiting
age=6480, PRODUCER1 produced item 0.821626
SCMP: send notify
SCMP: done waiting
SCMP: matching 2 messages against 1 receivers
SCMP: no matches found
SCMP: waiting
age=6590, Consumer1 wants to consume item less than 0.612647
SCMP: receive notify
SCMP: done waiting
SCMP: matching 2 messages against 2 receivers
SCMP: no matches found
SCMP: waiting
age=6700, Consumer0 wants to consume item less than 0.484274
SCMP: receive notify
SCMP: done waiting
SCMP: matching 2 messages against 3 receivers
SCMP: no matches found
SCMP: waiting
age()=7080, time to stop the threads and exit

D:\>java ProducersConsumers -A -R7
ProducersConsumers:
 synchronous=false, numProducers=2, numConsumers=3, pNap=2, cNap=3, runTime=7
age=60, PRODUCER0 napping for 1057 ms
age=110, PRODUCER1 napping for 1068 ms
age=110, Consumer0 napping for 1227 ms
All threads started
age=110, Consumer2 napping for 2901 ms
age=110, Consumer1 napping for 2854 ms
age=1150, PRODUCER0 produced item 0.966617
age=1150, PRODUCER0 deposited item 0.966617
age=1150, PRODUCER0 napping for 1631 ms
age=1150, PRODUCER1 produced item 0.460183
age=1150, PRODUCER1 deposited item 0.460183
age=1150, PRODUCER1 napping for 471 ms
age=1320, Consumer0 wants to consume item less than 0.419507
age=1650, PRODUCER1 produced item 0.796583
age=1650, PRODUCER1 deposited item 0.796583
age=1650, PRODUCER1 napping for 771 ms
age=2420, PRODUCER1 produced item 0.429785
age=2420, PRODUCER1 deposited item 0.429785
age=2420, PRODUCER1 napping for 1515 ms
age=2750, PRODUCER0 produced item 0.0392317
age=2750, PRODUCER0 deposited item 0.0392317
age=2750, PRODUCER0 napping for 1277 ms
age=2750, Consumer0 fetched item 0.0392317
age=2750, Consumer0 napping for 68 ms
age=2860, Consumer0 wants to consume item less than 0.385388
age=3020, Consumer1 wants to consume item less than 0.437923
age=3020, Consumer1 fetched item 0.429785
age=3020, Consumer1 napping for 2251 ms
age=3020, Consumer2 wants to consume item less than 0.272662
age=3960, PRODUCER1 produced item 0.21733
age=3960, PRODUCER1 deposited item 0.21733
age=3960, PRODUCER1 napping for 1622 ms
age=3960, Consumer0 fetched item 0.21733
age=3960, Consumer0 napping for 398 ms
age=4070, PRODUCER0 produced item 0.564506
age=4070, PRODUCER0 deposited item 0.564506
age=4070, PRODUCER0 napping for 1698 ms
age=4340, Consumer0 wants to consume item less than 0.303366
age=5270, Consumer1 wants to consume item less than 0.983944
age=5270, Consumer1 fetched item 0.966617
age=5270, Consumer1 napping for 2914 ms
age=5600, PRODUCER1 produced item 0.127829
age=5600, PRODUCER1 deposited item 0.127829
age=5600, PRODUCER1 napping for 962 ms
age=5600, Consumer2 fetched item 0.127829
age=5600, Consumer2 napping for 2523 ms
age=5770, PRODUCER0 produced item 0.590879
age=5770, PRODUCER0 deposited item 0.590879
age=5770, PRODUCER0 napping for 899 ms
age=6540, PRODUCER1 produced item 0.10659
age=6540, PRODUCER1 deposited item 0.10659
age=6540, PRODUCER1 napping for 321 ms
age=6540, Consumer0 fetched item 0.10659
age=6540, Consumer0 napping for 335 ms
age=6650, PRODUCER0 produced item 0.0534177
age=6650, PRODUCER0 deposited item 0.0534177
age=6650, PRODUCER0 napping for 1323 ms
age=6870, PRODUCER1 produced item 0.0367611
age=6870, PRODUCER1 deposited item 0.0367611
age=6870, PRODUCER1 napping for 995 ms
age=6920, Consumer0 wants to consume item less than 0.320468
age=6920, Consumer0 fetched item 0.0534177
age=6920, Consumer0 napping for 2139 ms
age()=7140, time to stop the threads and exit
                                            ... end of example run(s)  */
