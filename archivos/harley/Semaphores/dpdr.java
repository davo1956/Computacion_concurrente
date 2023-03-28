import Utilities.*;

class Philosopher extends MyObject implements Runnable {

   private int id = 0;
   private int napThink = 0; // both are in
   private int napEat = 0;   // milliseconds
   private DiningServer ds = null;

   public Philosopher(String name, int id, int napThink,
         int napEat, DiningServer ds) {
      super(name + " " + id);
      this.id = id;
      this.napThink = napThink;
      this.napEat = napEat;
      this.ds = ds;
      System.out.println(getName() + " is alive, napThink="
         + napThink + ", napEat=" + napEat);
      new Thread(this).start();
   }

   private void think() {
      int napping;
      napping = 1 + (int) random(napThink);
      System.out.println("age()=" + age() + ", " + getName()
         + " is thinking for " + napping + " ms");
      nap(napping);
   }

   private void eat() {
      int napping;
      napping = 1 + (int) random(napEat);
      System.out.println("age()=" + age() + ", " + getName()
         + " is eating for " + napping + " ms");
      nap(napping);
   }

   public void run() {
      while (true) {
         think();
         System.out.println("age()=" + age() + ", " + getName()
            + " wants to eat");
         ds.takeForks(id);
         eat();
         ds.putForks(id);
      }
   }
}

class DiningPhilosophers extends MyObject {

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Up:R:");
      go.optErr = true;
      String usage = "Usage: -p numPhilosophers"
         + " -R runTime napThink[i] napEat[i] i=0,1,...";
      int ch = -1;
      int numPhilosophers = 5;
      int runTime = 60;      // seconds
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'p') numPhilosophers =
            go.processArg(go.optArgGet(), numPhilosophers);
         else if ((char)ch == 'R')
            runTime = go.processArg(go.optArgGet(), runTime);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("DiningPhilosophers: numPhilosophers="
         + numPhilosophers + ", runTime=" + runTime);

      // process non-option command line arguments
      int[] napThink = new int[numPhilosophers];
      int[] napEat = new int[numPhilosophers];
      for (int i = 0; i < numPhilosophers; i++) {
         napThink[i] = 8; napEat[i] = 2;  // defaults
      }
      int argNum = go.optIndexGet();
      for (int i = 0; i < numPhilosophers; i++) {
         napThink[i] = go.tryArg(argNum++, napThink[i]);
         napEat[i] = go.tryArg(argNum++, napEat[i]);
      }

      // create the DiningServer object
      DiningServer ds = new DiningServer(numPhilosophers);

      // create the Philosophers (they have self-starting threads)
      for (int i = 0; i < numPhilosophers; i++)
         new Philosopher("Philosopher", i,
            napThink[i]*1000, napEat[i]*1000, ds);
      System.out.println("All Philosopher threads started");

      // let the Philosophers run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the Philosophers and exit");
      System.exit(0);
   }
}
