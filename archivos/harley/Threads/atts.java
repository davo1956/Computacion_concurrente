import Utilities.*;

class Node extends MyObject implements Runnable {

   private int id = 0;
   private int napOutsideCS = 0; // both are in
   private int napInsideCS = 0;  // milliseconds
   private Arbitrator arb = null;

   public Node(String name, int id, int napOutsideCS,
         int napInsideCS, Arbitrator arb) {
      super(name + " " + id);
      this.id = id;
      this.napOutsideCS = napOutsideCS;
      this.napInsideCS = napInsideCS;
      this.arb = arb;
      System.out.println(getName() + " is alive, napOutsideCS="
         + napOutsideCS + ", napInsideCS=" + napInsideCS);
      new Thread(this).start();
   }

   private void outsideCS() {
      int napping;
      napping = ((int) random(napOutsideCS)) + 1;
      System.out.println("age()=" + age() + ", " + getName()
         + " napping outside CS for " + napping + " ms");
      nap(napping);
   }

   private void insideCS() {
      int napping;
      napping = ((int) random(napInsideCS)) + 1;
      System.out.println("age()=" + age() + ", " + getName()
         + " napping inside CS for " + napping + " ms");
      nap(napping);
   }

   public void run() {
      while (true) {
         outsideCS();
         System.out.println("age()=" + age() + ", " + getName()
            + " wants to enter its CS");
         arb.wantToEnterCS(id);
         insideCS();
         arb.finishedInCS(id);
      }
   }
}

class MutualExclusion extends MyObject {

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Un:R:");
      go.optErr = true;
      String usage = "Usage: -n numNodes -R runTime napOutsideCS[i]"
         + " napInsideCS[i] i=0,1,...";
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
      System.out.println("MutualExclusion: numNodes=" + numNodes
         + ", runTime=" + runTime);

      // process non-option command line arguments
      int[] napOutsideCS = new int[numNodes];
      int[] napInsideCS = new int[numNodes];
      for (int i = 0; i < numNodes; i++) {
         napOutsideCS[i] = 8; napInsideCS[i] = 2;
      }
      int argNum = go.optIndexGet();
      for (int i = 0; i < numNodes; i++) {
         napOutsideCS[i] = go.tryArg(argNum++, napOutsideCS[i]);
         napInsideCS[i] = go.tryArg(argNum++, napInsideCS[i]);
      }

      // create the Arbitrator object
      Arbitrator arb = new Arbitrator(numNodes);

      // the Node threads are started in the constructor
      for (int i = 0; i < numNodes; i++)
         new Node("Node", i, napOutsideCS[i]*1000, napInsideCS[i]*1000, arb);
      System.out.println("All Node threads started");

      // let the Nodes run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the threads and exit");
      System.exit(0);
   }
}
