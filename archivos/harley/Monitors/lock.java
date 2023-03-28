import Utilities.*;

class Lock { // Lock is unlocked when owner == null
             // else lock is held by owner thread
   private Thread owner = null; // initial state is always unlocked

   public Lock() {super();}

   public synchronized void lock() {
      Thread who = Thread.currentThread();
      if (owner == who) return; // allow owner to relock silently
      else if (owner == null) {
         owner = who;
         return;
      } else /* owner != who && owner != null */ {
         while (owner != null) { // since ``barging'' is possible in
            try {                // Java monitors, a while loop is needed
               wait();           // even though only one thread is awakened
            } catch (InterruptedException e) {}
            // here `owner' can be null or another thread if it barged in
         }
         owner = who;
         return;
      }
   }

   public synchronized void unlock()
         throws IllegalMonitorStateException {
      Thread who = Thread.currentThread(); // allow unlocked lock
      if (owner == null) return;       // to be unlocked silently
      else if (owner != who) {
         throw new IllegalMonitorStateException();
      } else /* owner == who */ {
         owner = null;
         notify(); // at most one thread can proceed if
      }            // any are waiting so notifyAll() not
   }               // needed
}

class Node extends MyObject implements Runnable {

   private int id = 0;
   private int napOutsideLock = 0; // both are in
   private int napInsideLock = 0;  // milliseconds
   private Lock arb = null;

   public Node(String name, int id, int napOutsideLock,
         int napInsideLock, Lock arb) {
      super(name + " " + id);
      this.id = id;
      this.napOutsideLock = napOutsideLock;
      this.napInsideLock = napInsideLock;
      this.arb = arb;
      System.out.println(getName() + " is alive, napOutsideLock="
         + napOutsideLock + ", napInsideLock=" + napInsideLock);
      new Thread(this).start();
   }

   private void outsideLock() {
      int napping;
      napping = ((int) random(napOutsideLock)) + 1;
      System.out.println("age()=" + age() + ", " + getName()
         + " napping without lock for " + napping + " ms");
      nap(napping/2);
      if (random() >= 0.5) {
         System.out.println("age()=" + age() + ", " + getName()
            + " unlocking non-owned lock");
         try {
            arb.unlock();
         } catch (IllegalMonitorStateException e) {
            System.out.println("(outsideLock) " + e);
         }
      }
      nap(napping/2);
   }

   private void insideLock() {
      int napping;
      napping = ((int) random(napInsideLock)) + 1;
      System.out.println("age()=" + age() + ", " + getName()
         + " napping with lock for " + napping + " ms");
      nap(napping/2);
      if (random() >= 0.5) {
         System.out.println("age()=" + age() + ", " + getName()
            + " relocking lock");
         arb.lock();
      }
      nap(napping/2);
   }

   public void run() {
      while (true) {
         outsideLock();
         System.out.println("age()=" + age() + ", " + getName()
            + " wants to lock");
         arb.lock();
         insideLock();
         try {
            arb.unlock();
         } catch (IllegalMonitorStateException e) {
            System.out.println("(insideLock) " + e);
         }
      }
   }
}

class Locking extends MyObject {

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Un:R:");
      go.optErr = true;
      String usage = "Usage: -n numNodes -R runTime napOutsideLock[i]"
               + " napInsideLock[i] i=0,1,...";
      int ch = -1;
      int numNodes = 2;
      int runTime = 60;      // seconds
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'n')
            numNodes = go.processArg(go.optArgGet(), numNodes);
         else if ((char)ch == 'R')
            runTime = go.processArg(go.optArgGet(), runTime);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("Locking: numNodes=" + numNodes
         + ", runTime=" + runTime);

      // process non-option command line arguments
      int[] napOutsideLock = new int[numNodes];
      int[] napInsideLock = new int[numNodes];
      for (int i = 0; i < numNodes; i++) {
         napOutsideLock[i] = 8; napInsideLock[i] = 2;
      }
      int argNum = go.optIndexGet();
      for (int i = 0; i < numNodes; i++) {
         napOutsideLock[i] = go.tryArg(argNum++, napOutsideLock[i]);
         napInsideLock[i] = go.tryArg(argNum++, napInsideLock[i]);
      }

      // create the Lock object
      Lock arb = new Lock();

      // create the Nodes (self-starting threads)
      for (int i = 0; i < numNodes; i++)
         new Node("Node", i,
            napOutsideLock[i]*1000, napInsideLock[i]*1000, arb);
      System.out.println("All Node threads started");

      // let the Nodes run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the threads and exit");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac lock.java

D:\>java Locking -n5 -R10
Locking: numNodes=5, runTime=10
Node 0 is alive, napOutsideLock=8000, napInsideLock=2000
Node 1 is alive, napOutsideLock=8000, napInsideLock=2000
Node 2 is alive, napOutsideLock=8000, napInsideLock=2000
Node 3 is alive, napOutsideLock=8000, napInsideLock=2000
Node 4 is alive, napOutsideLock=8000, napInsideLock=2000
All Node threads started
age()=220, Node 1 napping without lock for 5033 ms
age()=220, Node 2 napping without lock for 5161 ms
age()=220, Node 3 napping without lock for 1795 ms
age()=220, Node 4 napping without lock for 2010 ms
age()=220, Node 0 napping without lock for 4523 ms
age()=1150, Node 3 unlocking non-owned lock
age()=2030, Node 3 wants to lock
age()=2030, Node 3 napping with lock for 144 ms
age()=2190, Node 3 napping without lock for 1327 ms
age()=2250, Node 4 wants to lock
age()=2250, Node 4 napping with lock for 1290 ms
age()=2520, Node 0 unlocking non-owned lock
(outsideLock) java.lang.IllegalMonitorStateException
age()=2740, Node 1 unlocking non-owned lock
(outsideLock) java.lang.IllegalMonitorStateException
age()=2910, Node 4 relocking lock
age()=3570, Node 3 wants to lock
age()=3570, Node 4 napping without lock for 533 ms
age()=3570, Node 3 napping with lock for 1529 ms
age()=3840, Node 4 unlocking non-owned lock
(outsideLock) java.lang.IllegalMonitorStateException
age()=4120, Node 4 wants to lock
age()=4770, Node 0 wants to lock
age()=5100, Node 3 napping without lock for 4510 ms
age()=5100, Node 4 napping with lock for 1948 ms
age()=5270, Node 1 wants to lock
age()=5380, Node 2 wants to lock
age()=6090, Node 4 relocking lock
age()=7080, Node 4 napping without lock for 3053 ms
age()=7080, Node 0 napping with lock for 2 ms
age()=7080, Node 0 relocking lock
age()=7080, Node 0 napping without lock for 5275 ms
age()=7080, Node 1 napping with lock for 540 ms
age()=7630, Node 1 napping without lock for 7259 ms
age()=7630, Node 2 napping with lock for 1593 ms
age()=8620, Node 4 unlocking non-owned lock
(outsideLock) java.lang.IllegalMonitorStateException
age()=9220, Node 2 napping without lock for 2431 ms
age()=9660, Node 3 wants to lock
age()=9660, Node 3 napping with lock for 157 ms
age()=9720, Node 3 relocking lock
age()=9720, Node 0 unlocking non-owned lock
(outsideLock) java.lang.IllegalMonitorStateException
age()=9830, Node 3 napping without lock for 6246 ms
age()=10160, Node 4 wants to lock
age()=10160, Node 4 napping with lock for 1920 ms
age()=10210, time to stop the threads and exit
                                            ... end of example run(s)  */
