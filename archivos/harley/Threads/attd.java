class Flag { public volatile boolean value = false; }

class Arbitrator {

   private static final int NUM_NODES = 2; // for two nodes only
                       // Dekker's solution:  take turns backing off.
   private Flag[] desiresCS = new Flag[NUM_NODES];
   private volatile int turn = 0;

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
      while (desiresCS[other(i)].value) {
         if (turn != i) {
            desiresCS[i].value = false;      // back off
            while (turn != i) /* busy wait */ Thread.currentThread().yield();
            desiresCS[i].value = true;
         }
      }
   }

   public void finishedInCS(int i) {        // post-protocol
      desiresCS[i].value = false;
      turn = other(i);
   }
}
