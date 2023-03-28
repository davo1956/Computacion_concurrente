import Utilities.*;

class Producer extends MyObject implements Runnable {

   private int id = 0;
   private int pNap = 0; // milliseconds
   private UnboundedBuffer ub = null;

   public Producer(String name, int id, int pNap, UnboundedBuffer ub) {
      super(name + " " + id);
      this.id = id;
      this.pNap = pNap;
      this.ub = ub;
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
         try {
            ub.deposit(item);
         } catch (CorruptedQueueException e) {
            System.err.println(e);
            System.exit(1);
         }
         System.out.println(getName() + " deposited item " + item);
      }
   }
}

class Consumer extends MyObject implements Runnable {

   private int id = 0;
   private int cNap = 0; // milliseconds
   private UnboundedBuffer ub = null;

   public Consumer(String name, int id, int cNap, UnboundedBuffer ub) {
      super(name + " " + id);
      this.id = id;
      this.cNap = cNap;
      this.ub = ub;
      new Thread(this).start();
   }

   public void run() {
      double item = 0;
      int napping;
      while (true) {
         napping = 1 + (int) random(cNap);
         System.out.println("age=" + age() + ", " + getName()
            + " napping for " + napping + " ms");
         nap(napping);
         System.out.println("age=" + age() + ", " + getName()
            + " wants to consume");
         try {
            item = ub.fetch();
         } catch (CorruptedQueueException e) {
            System.err.println(e);
            System.exit(1);
         }
         System.out.println(getName() + " fetched item " + item);
      }
   }
}

class ProducersConsumers extends MyObject {

   public static void main(String[] args) {

      // parse command line arguments, if any, to override defaults
      GetOpt go = new GetOpt(args, "UP:C:p:c:R:");
      go.optErr = true;
      String usage = "Usage: -P numP -C numC -p pNap -c cNap -R runTime";
      int ch = -1;
      int numProducers = 2;
      int numConsumers = 3;
      int pNap = 3;       // defaults
      int cNap = 2;       // in
      int runTime = 60;   // seconds
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
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
      System.out.println("ProducersConsumers: numProducers=" + numProducers
         + ", numConsumers=" + numConsumers
         + ", pNap=" + pNap + ", cNap=" + cNap + ", runTime=" + runTime);

      // create the unbounded buffer
      UnboundedBuffer ub = new UnboundedBuffer();

      // create the Producers and Consumers (self-starting threads)
      for (int i = 0; i < numProducers; i++)
         new Producer("PRODUCER", i, pNap*1000, ub);
      for (int i = 0; i < numConsumers; i++)
         new Consumer("Consumer", i, cNap*1000, ub);
      System.out.println("All threads started");

      // let them run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the threads and exit");
      System.exit(0);
   }
}
