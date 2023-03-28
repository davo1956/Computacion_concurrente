class Ticket { public volatile int value = 0; }

class Arbitrator {

   private static final int NUM_NODES = 2; // for two nodes only
                          // Lamport's bakery ticket algorithm.
   private Ticket[] ticket = new Ticket[NUM_NODES];

   public Arbitrator(int numNodes) {
      if (numNodes != NUM_NODES) {
         System.err.println("Arbitrator: numNodes=" + numNodes
            + " which is != " + NUM_NODES);
         System.exit(1);
      }
      for (int i = 0; i < NUM_NODES; i++) ticket[i] = new Ticket();
   }

   private int other(int i) { return (i + 1) % NUM_NODES; }

   public void wantToEnterCS(int i) {       // pre-protocol
      ticket[i].value = 1;
      ticket[i].value = ticket[other(i)].value + 1;  // compute next ticket
      while (!(ticket[other(i)].value == 0
         || ticket[i].value < ticket[other(i)].value
         || (ticket[i].value == ticket[other(i)].value  // break a tie
           && i == 0))) /* busy wait */ Thread.currentThread().yield();
   }

   public void finishedInCS(int i) {        // post-protocol
      ticket[i].value = 0;
   }
}
