import Utilities.*;
import Synchronization.*;

class Producer extends MyObject implements Runnable {

   private int pNap = 0; // milliseconds
   private MessagePassing mp = null;

   public Producer(String name, int pNap, MessagePassing mp) {
      super(name);
      this.pNap = pNap;
      this.mp = mp;
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
         send(mp, item);
         System.out.println("age=" + age() + ", " + getName()
            + " deposited item " + item);
      }
   }
}

class Consumer extends MyObject implements Runnable {

   private int cNap = 0; // milliseconds
   private MessagePassing mp = null;

   public Consumer(String name, int cNap, MessagePassing mp) {
      super(name);
      this.cNap = cNap;
      this.mp = mp;
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
         System.out.println("age=" + age() + ", " + getName()
            + " wants to consume");
         item = receiveDouble(mp);
         System.out.println("age=" + age() + ", " + getName()
            + " fetched item " + item);
      }
   }
}

class ProducersConsumers extends MyObject {

   private final static int ASYNC = 0, SYNC = 1, BB = 2, PIPED = 3,
      OBJPIPED = 4;

   public static void main(String[] args) {

      // parse command line arguments, if any, to override defaults
      GetOpt go = new GetOpt(args, "UASBIOP:C:p:c:R:");
      String usage = "Usage:"
        + " -A|S|B|I|O -P numP -C numC -p pNap -c cNap -R runTime";
      go.optErr = true;
      int ch = -1;
      int mpType = ASYNC;
      int numProducers = 2;
      int numConsumers = 3;
      int pNap = 2;       // defaults
      int cNap = 3;       // in
      int runTime = 60;   // seconds
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'A') mpType = ASYNC;
         else if ((char)ch == 'S') mpType = SYNC;
         else if ((char)ch == 'B') mpType = BB;
         else if ((char)ch == 'I') mpType = PIPED;
         else if ((char)ch == 'O') mpType = OBJPIPED;
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
      System.out.println("ProducersConsumers"
         + ", numProducers=" + numProducers
         + ", numConsumers=" + numConsumers
         + ", pNap=" + pNap + ", cNap=" + cNap + ", runTime=" + runTime);

      // Choose the kind of message passing.
      MessagePassing mp = null, mpS = null, mpR = null;
/*
 * AsyncMP is like a "bag of tasks" in which producer thread
 * put work to do and consumers get work to do; it is unbounded
 * in size and if there is no work, consumer threads block.
 */
      if (mpType == ASYNC) {
         mp = new AsyncMessagePassing();
         System.out.println("ASYNC message passing is being used.");
      }
/*
 * A BB is a "bag of tasks" with a finite size; if it fills
 * up, producer threads block (controlled capacity message passing channel).
 */
      else if (mpType == BB) {
         mp = new BBMessagePassing(5);
         System.out.println("BB(5 slots) message passing is being used.");
      }
/*
 * SyncMP is a "simple rendezvous" in which a producer thread
 * and a concumer thread meet for an instant at the same time and
 * at particular places in their code; the producer passes work
 * the consumer; a producer blocks until a consumer arrives and
 * vice versa.
 */
      else if (mpType == SYNC) {
         mp = new SyncMessagePassing();
         System.out.println("SYNC message passing is being used.");
      }
/*
 * PipedMP is like AsyncMP except that items (ints or doubles) are passed
 * through a pipe by value rather than through a Vector (linked list) by
 * reference.
 */
      else if (mpType == PIPED) {
         mp = new PipedMessagePassing();
         System.out.println("PIPED message passing is being used.");
      }
/*
 * ObjPipedMP is like PipedMP except that items (ints or doubles) are passed
 * through a pipe by value by creating wrapper objects and serializing them.
 */
      else if (mpType == OBJPIPED) {
         mp = new ObjPipedMessagePassing();
         System.out.println("OBJPIPED message passing is being used.");
      } else {
         System.err.println("Illegal type of message passing requested.");
         System.exit(1);
      }

      // create restricted rights message passing channels
      mpS = new MessagePassingSendOnly(mp);
      mpR = new MessagePassingReceiveOnly(mp);
      // create the Producers and Consumers (they start their own threads)
      for (int i = 0; i < numProducers; i++)
         new Producer("PRODUCER"+i, pNap*1000, mpS);
      for (int i = 0; i < numConsumers; i++)
         new Consumer("Consumer"+i, cNap*1000, mpR);
      System.out.println("All threads started");

      // let them run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the threads and exit");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac bagt.java

D:\>java ProducersConsumers -A -R5
ProducersConsumers, numProducers=2, numConsumers=3, pNap=2, cNap=3, runTime=5
ASYNC message passing is being used.
age=50, PRODUCER0 napping for 508 ms
age=50, PRODUCER1 napping for 923 ms
All threads started
age=110, Consumer0 napping for 21 ms
age=110, Consumer1 napping for 2728 ms
age=160, Consumer2 napping for 1322 ms
age=160, Consumer0 wants to consume
age=600, PRODUCER0 produced item 0.0265854
age=600, PRODUCER0 deposited item 0.0265854
age=600, PRODUCER0 napping for 195 ms
age=600, Consumer0 fetched item 0.0265854
age=600, Consumer0 napping for 2981 ms
age=770, PRODUCER0 produced item 0.122715
age=770, PRODUCER0 deposited item 0.122715
age=770, PRODUCER0 napping for 479 ms
age=990, PRODUCER1 produced item 0.507167
age=990, PRODUCER1 deposited item 0.507167
age=990, PRODUCER1 napping for 1467 ms
age=1260, PRODUCER0 produced item 0.782696
age=1260, PRODUCER0 deposited item 0.782696
age=1260, PRODUCER0 napping for 971 ms
age=1480, Consumer2 wants to consume
age=1480, Consumer2 fetched item 0.122715
age=1480, Consumer2 napping for 708 ms
age=2190, Consumer2 wants to consume
age=2190, Consumer2 fetched item 0.507167
age=2190, Consumer2 napping for 151 ms
age=2250, PRODUCER0 produced item 0.491896
age=2250, PRODUCER0 deposited item 0.491896
age=2250, PRODUCER0 napping for 545 ms
age=2360, Consumer2 wants to consume
age=2360, Consumer2 fetched item 0.782696
age=2360, Consumer2 napping for 784 ms
age=2470, PRODUCER1 produced item 0.852226
age=2470, PRODUCER1 deposited item 0.852226
age=2470, PRODUCER1 napping for 14 ms
age=2520, PRODUCER1 produced item 0.322477
age=2520, PRODUCER1 deposited item 0.322477
age=2520, PRODUCER1 napping for 973 ms
age=2800, PRODUCER0 produced item 0.37577
age=2800, PRODUCER0 deposited item 0.37577
age=2800, PRODUCER0 napping for 446 ms
age=2850, Consumer1 wants to consume
age=2850, Consumer1 fetched item 0.491896
age=2850, Consumer1 napping for 2821 ms
age=3130, Consumer2 wants to consume
age=3130, Consumer2 fetched item 0.852226
age=3130, Consumer2 napping for 1176 ms
age=3240, PRODUCER0 produced item 0.144847
age=3240, PRODUCER0 deposited item 0.144847
age=3240, PRODUCER0 napping for 1845 ms
age=3460, PRODUCER1 produced item 0.192217
age=3460, PRODUCER1 deposited item 0.192217
age=3460, PRODUCER1 napping for 850 ms
age=3570, Consumer0 wants to consume
age=3570, Consumer0 fetched item 0.322477
age=3570, Consumer0 napping for 2516 ms
age=4340, Consumer2 wants to consume
age=4340, Consumer2 fetched item 0.37577
age=4340, Consumer2 napping for 735 ms
age=4340, PRODUCER1 produced item 0.56971
age=4340, PRODUCER1 deposited item 0.56971
age=4340, PRODUCER1 napping for 816 ms
age=5050, Consumer2 wants to consume
age=5050, Consumer2 fetched item 0.144847
age=5050, Consumer2 napping for 2542 ms
age=5110, PRODUCER0 produced item 0.597646
age=5110, PRODUCER0 deposited item 0.597646
age=5110, PRODUCER0 napping for 1827 ms
age()=5110, time to stop the threads and exit

D:\>java ProducersConsumers -B -P7 -R5
ProducersConsumers, numProducers=7, numConsumers=3, pNap=2, cNap=3, runTime=5
BB(5 slots) message passing is being used.
age=60, PRODUCER0 napping for 447 ms
All threads started
age=110, PRODUCER1 napping for 1962 ms
age=110, PRODUCER3 napping for 253 ms
age=110, PRODUCER4 napping for 418 ms
age=110, PRODUCER5 napping for 502 ms
age=110, PRODUCER6 napping for 1343 ms
age=110, Consumer0 napping for 984 ms
age=110, Consumer1 napping for 1365 ms
age=110, Consumer2 napping for 125 ms
age=110, PRODUCER2 napping for 969 ms
age=220, Consumer2 wants to consume
age=330, PRODUCER3 produced item 0.582133
age=390, PRODUCER3 deposited item 0.582133
age=390, PRODUCER3 napping for 447 ms
age=390, Consumer2 fetched item 0.582133
age=390, Consumer2 napping for 226 ms
age=500, PRODUCER0 produced item 0.114251
age=500, PRODUCER0 deposited item 0.114251
age=500, PRODUCER0 napping for 1006 ms
age=550, PRODUCER4 produced item 0.360875
age=550, PRODUCER4 deposited item 0.360875
age=550, PRODUCER4 napping for 175 ms
age=610, PRODUCER5 produced item 0.112787
age=610, PRODUCER5 deposited item 0.112787
age=610, PRODUCER5 napping for 1831 ms
age=610, Consumer2 wants to consume
age=610, Consumer2 fetched item 0.114251
age=610, Consumer2 napping for 1692 ms
age=720, PRODUCER4 produced item 0.937172
age=720, PRODUCER4 deposited item 0.937172
age=720, PRODUCER4 napping for 980 ms
age=820, PRODUCER3 produced item 0.466748
age=820, PRODUCER3 deposited item 0.466748
age=820, PRODUCER3 napping for 736 ms
age=1100, PRODUCER2 produced item 0.0132638
age=1100, PRODUCER2 deposited item 0.0132638
age=1100, PRODUCER2 napping for 595 ms
age=1100, Consumer0 wants to consume
age=1100, Consumer0 fetched item 0.360875
age=1100, Consumer0 napping for 1050 ms
age=1480, PRODUCER6 produced item 0.285907
age=1480, PRODUCER6 deposited item 0.285907
age=1480, PRODUCER6 napping for 540 ms
age=1480, Consumer1 wants to consume
age=1480, Consumer1 fetched item 0.112787
age=1480, Consumer1 napping for 709 ms
age=1540, PRODUCER0 produced item 0.65451
age=1540, PRODUCER0 deposited item 0.65451
age=1540, PRODUCER0 napping for 1470 ms
age=1590, PRODUCER3 produced item 0.470133
age=1700, PRODUCER4 produced item 0.959461
age=1700, PRODUCER2 produced item 0.74344
age=2030, PRODUCER6 produced item 0.0316517
age=2090, PRODUCER1 produced item 0.724104
age=2140, Consumer0 wants to consume
age=2140, Consumer0 fetched item 0.937172
age=2140, Consumer0 napping for 2007 ms
age=2140, PRODUCER3 deposited item 0.470133
age=2140, PRODUCER3 napping for 864 ms
age=2200, Consumer1 wants to consume
age=2200, Consumer1 fetched item 0.466748
age=2200, Consumer1 napping for 583 ms
age=2200, PRODUCER4 deposited item 0.959461
age=2200, PRODUCER4 napping for 1872 ms
age=2310, Consumer2 wants to consume
age=2310, Consumer2 fetched item 0.0132638
age=2310, Consumer2 napping for 2430 ms
age=2310, PRODUCER2 deposited item 0.74344
age=2310, PRODUCER2 napping for 1383 ms
age=2470, PRODUCER5 produced item 0.489304
age=2800, Consumer1 wants to consume
age=2800, Consumer1 fetched item 0.285907
age=2800, Consumer1 napping for 1089 ms
age=2800, PRODUCER6 deposited item 0.0316517
age=2800, PRODUCER6 napping for 306 ms
age=3020, PRODUCER0 produced item 0.0605017
age=3020, PRODUCER3 produced item 0.67855
age=3130, PRODUCER6 produced item 0.0837361
age=3740, PRODUCER2 produced item 0.312082
age=3900, Consumer1 wants to consume
age=3900, Consumer1 fetched item 0.65451
age=3900, Consumer1 napping for 2793 ms
age=3900, PRODUCER1 deposited item 0.724104
age=3900, PRODUCER1 napping for 1387 ms
age=4070, PRODUCER4 produced item 0.799626
age=4180, Consumer0 wants to consume
age=4180, Consumer0 fetched item 0.470133
age=4180, Consumer0 napping for 2424 ms
age=4180, PRODUCER5 deposited item 0.489304
age=4180, PRODUCER5 napping for 813 ms
age=4780, Consumer2 wants to consume
age=4780, Consumer2 fetched item 0.959461
age=4780, Consumer2 napping for 92 ms
age=4780, PRODUCER0 deposited item 0.0605017
age=4780, PRODUCER0 napping for 147 ms
age=4830, Consumer2 wants to consume
age=4830, Consumer2 fetched item 0.74344
age=4830, Consumer2 napping for 307 ms
age=4830, PRODUCER3 deposited item 0.67855
age=4830, PRODUCER3 napping for 982 ms
age=4940, PRODUCER0 produced item 0.512372
age=5000, PRODUCER5 produced item 0.793301
age()=5110, time to stop the threads and exit

D:\>java ProducersConsumers -S -R5
ProducersConsumers, numProducers=2, numConsumers=3, pNap=2, cNap=3, runTime=5
SYNC message passing is being used.
All threads started
age=60, PRODUCER0 napping for 1732 ms
age=60, PRODUCER1 napping for 1870 ms
age=110, Consumer1 napping for 2084 ms
age=110, Consumer2 napping for 29 ms
age=110, Consumer0 napping for 2711 ms
age=170, Consumer2 wants to consume
age=1810, PRODUCER0 produced item 0.470945
age=1810, Consumer2 fetched item 0.470945
age=1810, Consumer2 napping for 1403 ms
age=1810, PRODUCER0 deposited item 0.470945
age=1810, PRODUCER0 napping for 1414 ms
age=1980, PRODUCER1 produced item 0.9734
age=2200, Consumer1 wants to consume
age=2200, Consumer1 fetched item 0.9734
age=2200, Consumer1 napping for 962 ms
age=2200, PRODUCER1 deposited item 0.9734
age=2200, PRODUCER1 napping for 397 ms
age=2580, PRODUCER1 produced item 0.0789178
age=2860, Consumer0 wants to consume
age=2860, Consumer0 fetched item 0.0789178
age=2860, Consumer0 napping for 612 ms
age=2860, PRODUCER1 deposited item 0.0789178
age=2860, PRODUCER1 napping for 299 ms
age=3130, PRODUCER1 produced item 0.85063
age=3130, Consumer1 wants to consume
age=3130, Consumer1 fetched item 0.85063
age=3130, Consumer1 napping for 1357 ms
age=3130, PRODUCER1 deposited item 0.85063
age=3130, PRODUCER1 napping for 1743 ms
age=3240, Consumer2 wants to consume
age=3240, PRODUCER0 produced item 0.113369
age=3240, Consumer2 fetched item 0.113369
age=3240, Consumer2 napping for 2559 ms
age=3240, PRODUCER0 deposited item 0.113369
age=3240, PRODUCER0 napping for 1491 ms
age=3460, Consumer0 wants to consume
age=4510, Consumer1 wants to consume
age=4780, PRODUCER0 produced item 0.582764
age=4780, Consumer0 fetched item 0.582764
age=4780, Consumer0 napping for 1971 ms
age=4780, PRODUCER0 deposited item 0.582764
age=4780, PRODUCER0 napping for 223 ms
age=4890, PRODUCER1 produced item 0.931615
age=4890, Consumer1 fetched item 0.931615
age=4890, Consumer1 napping for 2223 ms
age=4890, PRODUCER1 deposited item 0.931615
age=4890, PRODUCER1 napping for 355 ms
age=5000, PRODUCER0 produced item 0.677571
age()=5050, time to stop the threads and exit

D:\>java ProducersConsumers -I -R5
Java version=1.1.1, Java vendor=Sun Microsystems Inc.
OS name=Windows 95, OS arch=x86, OS version=4.0
Thu Jun 19 16:12:44 EDT 1997
ProducersConsumers, numProducers=2, numConsumers=3, pNap=2, cNap=3, runTime=5
PIPED message passing is being used.
All threads started
age=110, PRODUCER0 napping for 1342 ms
age=110, PRODUCER1 napping for 1294 ms
age=170, Consumer0 napping for 1203 ms
age=170, Consumer1 napping for 2638 ms
age=170, Consumer2 napping for 2129 ms
age=1320, Consumer0 wants to consume
age=1430, PRODUCER1 produced item 0.22369513628165194
age=1430, PRODUCER1 deposited item 0.22369513628165194
age=1430, Consumer0 fetched item 0.22369513628165194
age=1430, PRODUCER1 napping for 1473 ms
age=1430, Consumer0 napping for 623 ms
age=1490, PRODUCER0 produced item 0.07209637015916348
age=1490, PRODUCER0 deposited item 0.07209637015916348
age=1490, PRODUCER0 napping for 1105 ms
age=2090, Consumer0 wants to consume
age=2090, Consumer0 fetched item 0.07209637015916348
age=2090, Consumer0 napping for 2653 ms
age=2250, Consumer2 wants to consume
age=2580, PRODUCER0 produced item 0.34195445343406783
age=2580, PRODUCER0 deposited item 0.34195445343406783
age=2580, PRODUCER0 napping for 936 ms
age=2580, Consumer2 fetched item 0.34195445343406783
age=2580, Consumer2 napping for 1166 ms
age=2800, Consumer1 wants to consume
age=2910, PRODUCER1 produced item 0.2936226801503907
age=2910, PRODUCER1 deposited item 0.2936226801503907
age=2910, PRODUCER1 napping for 1094 ms
age=2910, Consumer1 fetched item 0.2936226801503907
age=2910, Consumer1 napping for 1056 ms
age=3520, PRODUCER0 produced item 0.8611556604171742
age=3520, PRODUCER0 deposited item 0.8611556604171742
age=3520, PRODUCER0 napping for 1021 ms
age=3790, Consumer2 wants to consume
age=3790, Consumer2 fetched item 0.8611556604171742
age=3790, Consumer2 napping for 203 ms
age=3960, Consumer2 wants to consume
age=4010, Consumer1 wants to consume
age=4010, PRODUCER1 produced item 0.7981200854087825
age=4010, PRODUCER1 deposited item 0.7981200854087825
age=4010, PRODUCER1 napping for 460 ms
age=4010, Consumer2 fetched item 0.7981200854087825
age=4010, Consumer2 napping for 1452 ms
age=4510, PRODUCER1 produced item 0.965365321461567
age=4510, Consumer1 fetched item 0.965365321461567
age=4510, Consumer1 napping for 1245 ms
age=4510, PRODUCER1 deposited item 0.965365321461567
age=4510, PRODUCER1 napping for 1849 ms
age=4560, PRODUCER0 produced item 0.07925325495078062
age=4560, PRODUCER0 deposited item 0.07925325495078062
age=4560, PRODUCER0 napping for 635 ms
age=4730, Consumer0 wants to consume
age=4730, Consumer0 fetched item 0.07925325495078062
age=4730, Consumer0 napping for 2778 ms
age()=5110, time to stop the threads and exit

D:\>java ProducersConsumers -O -R5
Java version=1.1.1, Java vendor=Sun Microsystems Inc.
OS name=Windows 95, OS arch=x86, OS version=4.0
Thu Jun 19 16:47:06 EDT 1997
ProducersConsumers, numProducers=2, numConsumers=3, pNap=2, cNap=3, runTime=5
OBJPIPED message passing is being used.
age=170, PRODUCER0 napping for 1581 ms
All threads started
age=220, Consumer0 napping for 808 ms
age=220, PRODUCER1 napping for 1618 ms
age=220, Consumer1 napping for 2600 ms
age=220, Consumer2 napping for 2862 ms
age=1040, Consumer0 wants to consume
age=1760, PRODUCER0 produced item 0.06330446046658389
age=1810, PRODUCER0 deposited item 0.06330446046658389
age=1810, PRODUCER0 napping for 1925 ms
age=1810, Consumer0 fetched item 0.06330446046658389
age=1810, Consumer0 napping for 1346 ms
age=1870, PRODUCER1 produced item 0.49185285293407166
age=1870, PRODUCER1 deposited item 0.49185285293407166
age=1870, PRODUCER1 napping for 625 ms
age=2470, PRODUCER1 produced item 0.5999818916832333
age=2470, PRODUCER1 deposited item 0.5999818916832333
age=2470, PRODUCER1 napping for 798 ms
age=2860, Consumer1 wants to consume
age=2860, Consumer1 fetched item 0.49185285293407166
age=2860, Consumer1 napping for 758 ms
age=3080, Consumer2 wants to consume
age=3080, Consumer2 fetched item 0.5999818916832333
age=3080, Consumer2 napping for 1905 ms
age=3130, Consumer0 wants to consume
age=3300, PRODUCER1 produced item 0.8839208105989371
age=3300, PRODUCER1 deposited item 0.8839208105989371
age=3300, PRODUCER1 napping for 1184 ms
age=3300, Consumer0 fetched item 0.8839208105989371
age=3300, Consumer0 napping for 2765 ms
age=3570, Consumer1 wants to consume
age=3740, PRODUCER0 produced item 0.9007725064879978
age=3740, PRODUCER0 deposited item 0.9007725064879978
age=3740, PRODUCER0 napping for 1284 ms
age=3740, Consumer1 fetched item 0.9007725064879978
age=3740, Consumer1 napping for 2427 ms
age=4450, PRODUCER1 produced item 0.2681807538089772
age=4500, PRODUCER1 deposited item 0.2681807538089772
age=4500, PRODUCER1 napping for 1546 ms
age=5000, Consumer2 wants to consume
age=5000, Consumer2 fetched item 0.2681807538089772
age=5000, Consumer2 napping for 2428 ms
age=5000, PRODUCER0 produced item 0.6075475323852872
age=5000, PRODUCER0 deposited item 0.6075475323852872
age=5000, PRODUCER0 napping for 600 ms
age()=5220, time to stop the threads and exit
                                            ... end of example run(s)  */
