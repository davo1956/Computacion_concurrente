class Ticket { public volatile int value = 0; }

class Arbitrator {

   private int numNodes = 0; // Lamport's bakery ticket algorithm.
   private Ticket[] ticket = null;

   public Arbitrator(int numNodes) {
      this.numNodes = numNodes;
      ticket = new Ticket[numNodes];
      for (int i = 0; i < numNodes; i++) ticket[i] = new Ticket();
   }

   private int  maxx(Ticket[] ticket) {
      int mx = ticket[0].value;
      for (int i = 1; i < ticket.length; i++)
         if (ticket[i].value > mx) mx = ticket[i].value;
      return mx;
   }

   public void wantToEnterCS(int i) {       // pre-protocol
      ticket[i].value = 1;
      ticket[i].value = 1 + maxx(ticket);   // compute next ticket
      for (int j = 0; j < numNodes; j++) if (j != i)
         while (!(ticket[j].value == 0 || ticket[i].value < ticket[j].value
            || // break a tie
         (ticket[i].value == ticket[j].value && i < j))) // busy wait
            Thread.currentThread().yield();
   }

   public void finishedInCS(int i) {        // post-protocol
      ticket[i].value = 0;
   }
}

/* ............... Example compile and run(s)

D:\>javac atts.java bakn.java

D:\>java MutualExclusion -R15 -n4 1 1 1 1 8 3 8 3
MutualExclusion: numNodes=4, runTime=15
Node 0 is alive, napOutsideCS=1000, napInsideCS=1000
Node 1 is alive, napOutsideCS=1000, napInsideCS=1000
Node 2 is alive, napOutsideCS=8000, napInsideCS=3000
Node 3 is alive, napOutsideCS=8000, napInsideCS=3000
All Node threads started
age()=110, Node 0 napping outside CS for 661 ms
age()=110, Node 1 napping outside CS for 603 ms
age()=170, Node 2 napping outside CS for 834 ms
age()=170, Node 3 napping outside CS for 734 ms
age()=830, Node 0 wants to enter its CS
age()=830, Node 0 napping inside CS for 456 ms
age()=830, Node 1 wants to enter its CS
age()=990, Node 3 wants to enter its CS
age()=1100, Node 2 wants to enter its CS
age()=1320, Node 0 napping outside CS for 111 ms
age()=1380, Node 1 napping inside CS for 971 ms
age()=1490, Node 0 wants to enter its CS
age()=2370, Node 1 napping outside CS for 575 ms
age()=2530, Node 3 napping inside CS for 255 ms
age()=2860, Node 3 napping outside CS for 5523 ms
age()=2860, Node 2 napping inside CS for 2610 ms
age()=2970, Node 1 wants to enter its CS
age()=5500, Node 2 napping outside CS for 109 ms
age()=5500, Node 0 napping inside CS for 605 ms
age()=5610, Node 2 wants to enter its CS
age()=6100, Node 0 napping outside CS for 908 ms
age()=6150, Node 1 napping inside CS for 575 ms
age()=6760, Node 1 napping outside CS for 465 ms
age()=6760, Node 2 napping inside CS for 2593 ms
age()=7030, Node 0 wants to enter its CS
age()=7250, Node 1 wants to enter its CS
age()=8410, Node 3 wants to enter its CS
age()=9400, Node 2 napping outside CS for 2989 ms
age()=9450, Node 0 napping inside CS for 218 ms
age()=9730, Node 0 napping outside CS for 890 ms
age()=9780, Node 1 napping inside CS for 672 ms
age()=10440, Node 1 napping outside CS for 349 ms
age()=10440, Node 3 napping inside CS for 88 ms
age()=10550, Node 3 napping outside CS for 6790 ms
age()=10600, Node 0 wants to enter its CS
age()=10600, Node 0 napping inside CS for 342 ms
age()=10820, Node 1 wants to enter its CS
age()=10990, Node 0 napping outside CS for 719 ms
age()=10990, Node 1 napping inside CS for 582 ms
age()=11590, Node 1 napping outside CS for 592 ms
age()=11700, Node 0 wants to enter its CS
age()=11700, Node 0 napping inside CS for 876 ms
age()=12200, Node 1 wants to enter its CS
age()=12420, Node 2 wants to enter its CS
age()=12640, Node 0 napping outside CS for 670 ms
age()=12640, Node 1 napping inside CS for 9 ms
age()=12690, Node 1 napping outside CS for 519 ms
age()=12690, Node 2 napping inside CS for 2827 ms
age()=13190, Node 1 wants to enter its CS
age()=13350, Node 0 wants to enter its CS
age()=15160, time to stop the threads and exit
                                            ... end of example run(s)  */
