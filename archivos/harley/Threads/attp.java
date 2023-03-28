class Flag { public volatile boolean value = false; }

class Arbitrator {

   private static final int NUM_NODES = 2; // for two nodes only

   private Flag[] desiresCS = new Flag[NUM_NODES];
   private volatile int last = 0;          // Peterson's solution.

   public Arbitrator(int numNodes) {
      if (numNodes != NUM_NODES) {
         System.err.println("Arbitrator: numNodes=" + numNodes
            + " which is != " + NUM_NODES);
         System.exit(1);
      }
      for (int i = 0; i < NUM_NODES; i++) desiresCS[i] = new Flag();
   }

   private int other(int i) { return (i + 1) % NUM_NODES; }

   public void wantToEnterCS(int i) {       // pre-protocol
      desiresCS[i].value = true;
      last = i;
      while (desiresCS[other(i)].value && last == i) // busy wait
         Thread.currentThread().yield();
   }

   public void finishedInCS(int i) {        // post-protocol
      desiresCS[i].value = false;
   }
}

/* ............... Example compile and run(s)

D:\>javac atts.java attp.java

D:\>java MutualExclusion -R10 2 1 4 3
MutualExclusion: numNodes=2, runTime=10
Node 0 is alive, napOutsideCS=2000, napInsideCS=1000
Node 1 is alive, napOutsideCS=4000, napInsideCS=3000
All Node threads started
age()=160, Node 0 napping outside CS for 1630 ms
age()=160, Node 1 napping outside CS for 2619 ms
age()=1810, Node 0 wants to enter its CS
age()=1810, Node 0 napping inside CS for 212 ms
age()=2030, Node 0 napping outside CS for 538 ms
age()=2580, Node 0 wants to enter its CS
age()=2580, Node 0 napping inside CS for 664 ms
age()=2800, Node 1 wants to enter its CS
age()=3240, Node 0 napping outside CS for 1652 ms
age()=3240, Node 1 napping inside CS for 21 ms
age()=3290, Node 1 napping outside CS for 2292 ms
age()=4890, Node 0 wants to enter its CS
age()=4890, Node 0 napping inside CS for 189 ms
age()=5110, Node 0 napping outside CS for 1999 ms
age()=5550, Node 1 wants to enter its CS
age()=5550, Node 1 napping inside CS for 1917 ms
age()=7080, Node 0 wants to enter its CS
age()=7520, Node 1 napping outside CS for 3314 ms
age()=7520, Node 0 napping inside CS for 332 ms
age()=7850, Node 0 napping outside CS for 6 ms
age()=7850, Node 0 wants to enter its CS
age()=7850, Node 0 napping inside CS for 88 ms
age()=7960, Node 0 napping outside CS for 93 ms
age()=8070, Node 0 wants to enter its CS
age()=8070, Node 0 napping inside CS for 840 ms
age()=8900, Node 0 napping outside CS for 1811 ms
age()=10160, time to stop the threads and exit
                                            ... end of example run(s)  */
