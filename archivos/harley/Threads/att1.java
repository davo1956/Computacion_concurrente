class Arbitrator {

   private static final int NUM_NODES = 2; // for two nodes only

   private volatile int turn = 0;  // First attempt:  strict alternation.

   public Arbitrator(int numNodes) {
      if (numNodes != NUM_NODES) {
         System.err.println("Arbitrator: numNodes=" + numNodes
            + " which is != " + NUM_NODES);
         System.exit(1);
      }
   }

   private int other(int i) { return (i + 1) % NUM_NODES; }

   public void wantToEnterCS(int i) {       // pre-protocol
      while (turn != i) /* busy wait */ Thread.currentThread().yield();
   }

   public void finishedInCS(int i) {        // post-protocol
      turn = other(i);
   }
}

/* ............... Example compile and run(s)

D:\>javac atts.java att1.java

D:\>java MutualExclusion -U
Usage: -n numNodes -R runTime napOutsideCS[i] napInsideCS[i] i=0,1,...

D:\>java MutualExclusion -n 3
MutualExclusion: numNodes=3, runTime=60
tryArg: no theArgs[2]
tryArg: no theArgs[3]
tryArg: no theArgs[4]
tryArg: no theArgs[5]
tryArg: no theArgs[6]
tryArg: no theArgs[7]
Arbitrator: numNodes=3 which is != 2

D:\>java MutualExclusion -R10 2 1 4 3
MutualExclusion: numNodes=2, runTime=10
Node 0 is alive, napOutsideCS=2000, napInsideCS=1000
Node 1 is alive, napOutsideCS=4000, napInsideCS=3000
All Node threads started
age()=50, Node 0 napping outside CS for 1705 ms
age()=50, Node 1 napping outside CS for 1104 ms
age()=1210, Node 1 wants to enter its CS
age()=1810, Node 0 wants to enter its CS
age()=1810, Node 0 napping inside CS for 282 ms
age()=2080, Node 0 napping outside CS for 806 ms
age()=2080, Node 1 napping inside CS for 2276 ms
age()=2910, Node 0 wants to enter its CS
age()=4390, Node 1 napping outside CS for 2738 ms
age()=4390, Node 0 napping inside CS for 783 ms
age()=5220, Node 0 napping outside CS for 194 ms
age()=5380, Node 0 wants to enter its CS
age()=7190, Node 1 wants to enter its CS
age()=7190, Node 1 napping inside CS for 57 ms
age()=7250, Node 1 napping outside CS for 3576 ms
age()=7250, Node 0 napping inside CS for 938 ms
age()=8180, Node 0 napping outside CS for 183 ms
age()=8400, Node 0 wants to enter its CS
age()=10100, time to stop the threads and exit
                                            ... end of example run(s)  */
