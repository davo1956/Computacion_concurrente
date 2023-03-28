class Flag { public volatile boolean value = false; }

class Arbitrator {

   private static final int NUM_NODES = 2; // for two nodes only

                            // Fourth attempt:  back off.
   private Flag[] desiresCS = new Flag[NUM_NODES];

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
         desiresCS[i].value = false;      // back off
         Thread.currentThread().yield();
         desiresCS[i].value = true;
      }
   }

   public void finishedInCS(int i) {        // post-protocol
      desiresCS[i].value = false;
   }
}
