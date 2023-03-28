import java.awt.*;

class Philosopher extends MyObject implements Runnable {

   private int id = 0;
   private int napThink = 0; // both are in
   private int napEat = 0;   // milliseconds
   private DiningServer ds = null;
   private XtangoApplet xa = null;
   private Thread me = null;

   public Philosopher(String name, int id, int napThink,
         int napEat, DiningServer ds, XtangoApplet xa) {
      super(name + " " + id);
      this.id = id;
      this.napThink = napThink;
      this.napEat = napEat;
      this.ds = ds;
      this.xa = xa;
      System.out.println(getName() + " is alive, napThink="
         + napThink + ", napEat=" + napEat);
      me = new Thread(this);  me.start();
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
      xa.color("phil"+id, Color.blue);                 // animation
      xa.fill("phil"+id, xa.SOLID);                    // animation
      nap(napping);
   }

   public void run() {
      while (true) {
         think();
         System.out.println("age()=" + age() + ", " + getName()
            + " wants to eat");
         xa.color("phil"+id, Color.green);             // animation
         xa.fill("phil"+id, xa.SOLID);                 // animation
         ds.takeForks(id);
         eat();
         ds.putForks(id);
         xa.fill("phil"+id, xa.OUTLINE);               // animation
         xa.color("phil"+id, Color.black);             // animation
      }
   }

   public void stop() { me.stop(); }
}

class DiningServer extends MyObject {

   private static final int
      THINKING = 0, HUNGRY = 1, EATING = 2;
   private int numPhils = 0;
   private int[] state = null;
   private BinarySemaphore[] self = null;
   private BinarySemaphore mutex = null;
   private XtangoApplet xa = null;

   public DiningServer(int numPhils, XtangoApplet xa) {
      this.numPhils = numPhils;
      this.xa = xa;
      state = new int[numPhils];
      for (int i = 0; i < numPhils; i++) state[i] = THINKING;
      self = new BinarySemaphore[numPhils];
      for (int i = 0; i < numPhils; i++) self[i] = new BinarySemaphore(0);
      mutex = new BinarySemaphore(1);
   }

   private final int right(int i) { return (numPhils + i - 1) % numPhils; }

   private final int left(int i) { return (i + 1) % numPhils; }

   public void takeForks(int i) {
      P(mutex);
      state[i] = HUNGRY;
      test(i);
      V(mutex);
      P(self[i]);
      xa.color("fork"+i, Color.gray);                  // animation
      xa.color("fork"+right(i), Color.gray);           // animation
   }

   public void putForks(int i) {
      P(mutex);
      state[i] = THINKING;
      xa.color("fork"+i, Color.white);                 // animation
      xa.color("fork"+right(i), Color.white);          // animation
      test(left(i));
      test(right(i));
      V(mutex);
   }

   private void test(int k) {
      if (state[left(k)] != EATING && state[right(k)] != EATING
            && state[k] == HUNGRY) {
         state[k] = EATING;
         V(self[k]);
      }
   }
}

public class AnimatedDiningPhilosophers extends MyObject {

   private static final double TWO_PI = 2.0 * Math.PI;
   private static XtangoApplet xa = null;                // animation...

   public AnimatedDiningPhilosophers(XtangoApplet a) { xa = a; }

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
      System.out.println("AnimatedDP: numPhilosophers="
         + numPhilosophers + ", runTime=" + runTime);

      xa.begin();
      xa.coords(-1.5f, -1.5f, 1.5f, 1.5f);
      // circular table
      xa.circle("C0", 0.0f, 0.0f, (float)1, Color.black, xa.OUTLINE);
      // bowl of spaghetti
      xa.circle("Cf", 0.0f, 0.0f, 0.5f, Color.orange, xa.HALF);
      // legend
      xa.circle("C1", -1.4f, (float)-1, 0.05f, Color.black, xa.OUTLINE);
      xa.text("T1", -1.3f, -1.025f, false, Color.black, "THINKING");
      xa.circle("C2", -1.4f, -1.2f, 0.05f, Color.green, xa.SOLID);
      xa.text("T2", -1.3f, -1.225f, false, Color.black, "HUNGRY");
      xa.circle("C3", -1.4f, -1.4f, 0.05f, Color.blue, xa.SOLID);
      xa.text("T3", -1.3f, -1.425f, false, Color.black, "EATING");
                                                       // ...animation
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
      DiningServer ds = new DiningServer(numPhilosophers, xa);

      // create the Philosophers with self-starting threads
      Philosopher[] phil = new Philosopher[numPhilosophers];
      float gap = (float) TWO_PI/numPhilosophers;
      for (int i = 0; i < numPhilosophers; i++) {
         double radianp = i*gap;                       // animation...
         float sinp = (float) Math.sin(radianp);
         float cosp = (float) Math.cos(radianp);
         double radianf = radianp + 0.5f*gap;
         float sinf = (float) Math.sin(radianf);
         float cosf = (float) Math.cos(radianf);
         xa.circle("phil"+i, sinp, cosp, 0.3f*gap, Color.black, xa.OUTLINE);
         xa.bigText("TP"+i, sinp*0.55f, cosp*0.55f, true, Color.black, ""+i);
         // philosopher 1 is to the left of philosopher 0
         // fork 0 is to the left of philosopher 0
         xa.pointLine("fork"+i, sinf, cosf, 0.4f*sinf, 0.4f*cosf,
            Color.white, xa.THICK);
         xa.lower("fork"+i);                           // ...animation

         phil[i] = new Philosopher("Philosopher", i,
            napThink[i]*1000, napEat[i]*1000, ds, xa);
      }
      System.out.println("All Philosopher threads started");

      // let the Philosophers run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the Philosophers and exit");
      for (int i = 0; i < numPhilosophers; i++) phil[i].stop();
      xa.end();
   }
}
