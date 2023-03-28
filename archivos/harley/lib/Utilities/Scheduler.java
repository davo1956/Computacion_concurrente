package Utilities;

public class Scheduler extends MyObject implements Runnable {

   private int timeSlice = 0; // milliseconds
   private boolean randomSlice = false;
   private boolean debug = false;
   private Thread t = null;

   public Scheduler(int timeSlice, boolean randomSlice, boolean debug) {
      super("Scheduler");
      this.timeSlice = timeSlice;
      this.randomSlice = randomSlice;
      this.debug = debug;
      t = new Thread(this);
      t.setPriority(Thread.MAX_PRIORITY);
      t.setDaemon(true);
      t.start();
   }

   public Scheduler(int timeSlice) { this(timeSlice, false, false); }

   public void run() {
      int napping;
      System.out.println(getName() + ": timeSlice=" + timeSlice
         + " randomSlice=" + randomSlice + " priority=" + t.getPriority());
      while (true) {
         if (randomSlice) napping = 1 + (int) (2 * random(timeSlice));
         else napping = timeSlice;
         nap(napping);
         // this highest-priority thread waking up sends the currently
         // executing thread to the end of the round-robin ready queue
         if (debug)
            System.out.println(getName() + " slicing at time " + age());
      }
   }
}
