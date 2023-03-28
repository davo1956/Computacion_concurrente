import java.awt.*;

// message types
class Hungry {} // philosopher sends this to its servant
class NeedL {}  // servants
class NeedR {}  // send
class PassL {}  // these to
class PassR {}  // each other

class DistributedPhilosopher extends MyObject implements Runnable {

   private int id = 0;
   private int napThink = 0; // both are in
   private int napEat = 0;   // milliseconds
   private Servant myServant = null;
   private Thread me = null;

   public DistributedPhilosopher(String name, int id, int napThink,
         int napEat, Servant myServant) {
      super(name + " " + id);
      this.id = id;
      this.napThink = napThink;
      this.napEat = napEat;
      this.myServant = myServant;
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
      nap(napping);
   }

   public void run() {
      while (true) {
         think();
         System.out.println("age()=" + age() + ", " + getName()
            + " wants to eat");
         myServant.takeForks(id);
         eat();
         myServant.putForks(id);
      }
   }

   public void stop() { me.stop(); }
}

class ServantCondition extends MyObject implements Condition {

   private boolean hungry = false;
   private boolean dirtyL = false, dirtyR = false;

   public ServantCondition(boolean hungry, boolean dirtyL, boolean dirtyR) {
      super("ServantCondition: hungry=" + hungry
         + " dirtyL=" + dirtyL + " dirtyR=" + dirtyR);
      this.hungry = hungry;
      this.dirtyL = dirtyL;
      this.dirtyR = dirtyR;
   }

   public boolean checkCondition(Object m) {
      if (m instanceof Hungry) return true;
      else if (!hungry) return true;
      else if (m instanceof PassL || m instanceof PassR) return true;
      else if (m instanceof NeedL && dirtyL) return true;
      else if (m instanceof NeedR && dirtyR) return true;
      else return false;
   }
}

class Servant extends MyObject implements Runnable {

   // This group of fields is initialized by the constructor.
   private int id = 0;
   private AsyncConditionalMessagePassing myChannel = null;
   private AsyncConditionalMessagePassing leftServantChannel = null;
   private AsyncConditionalMessagePassing rightServantChannel = null;
   private boolean haveL = false, dirtyL = false;
   private boolean haveR = false, dirtyR = false;
   private XtangoApplet xa = null;
   private String forkL = null, forkR = null;
   private String holderL = null, holderR = null;

   private BinarySemaphore eat = new BinarySemaphore(0);
   private BinarySemaphore releaseForks = new BinarySemaphore(0);

   public Servant(String name, int id,
         String forkL, String forkR, String holderL, String holderR,
         boolean haveL, boolean dirtyL, boolean haveR, boolean dirtyR,
         AsyncConditionalMessagePassing myChannel,
         AsyncConditionalMessagePassing leftServantChannel,
         AsyncConditionalMessagePassing rightServantChannel,
         XtangoApplet xa) {
      super(name + " " + id);
      this.id = id;
      this.haveL = haveL; this.dirtyL = dirtyL;
      this.haveR = haveR; this.dirtyR = dirtyR;
      this.forkL = forkL; this.forkR = forkR;
      this.holderL = holderL; this.holderR = holderR;
      this.myChannel = myChannel;
      this.leftServantChannel = leftServantChannel;
      this.rightServantChannel = rightServantChannel;
      this.xa = xa;
      System.out.println("Servant " + getName() + " is alive:\n"
         + "forkL=" + forkL + ", forkR=" + forkR
         + ", holderL=" + holderL + ", holderR=" + holderR + "\n"
         + "haveL=" + haveL + ", haveR=" + haveR
         + ", dirtyL=" + dirtyL + ", dirtyR=" + dirtyR);
      new Thread(this).start();
   }

   public void takeForks(int id) {
      xa.color("phil"+id, Color.green);                // animation
      xa.fill("phil"+id, xa.SOLID);                    // animation
      myChannel.send(new Hungry());  // non blocking
      P(eat);                  // wait for empty message
      xa.color("phil"+id, Color.blue);                 // animation
      xa.fill("phil"+id, xa.SOLID);                    // animation
   }

   public void putForks(int id) {

      xa.fill("phil"+id, xa.OUTLINE);                  // animation
      xa.color("phil"+id, Color.black);                // animation
      V(releaseForks);         // send empty message
   }
/*
 * Some animation ideas: when a hungry philosopher receives a fork, change
 * its color to HALF (green or red); when a hungry philosopher has no forks
 * and it gets asked for one...cannot happen...; when a hungry philosopher
 * gets asked for a dirty fork it has, change its color to SOLID red for
 * frustration since it has to give it up.
 */

   public void run() {
// Move a left or right fork initially given to this philosopher to be next
// to the philosopher.
      if (haveR) xa.jumpTo(forkR, holderR);          // animation...
      if (haveL) xa.jumpTo(forkL, holderL);
      if (dirtyR) {
         xa.color(forkR, Color.orange);
      }
      if (dirtyL) {
         xa.color(forkL, Color.orange);
      }                                              // ...animation
      Object message = null;
      ServantCondition sc = null;
      boolean hungry = false;
      while (true) {
         sc = new ServantCondition(hungry, dirtyL, dirtyR);
         message = myChannel.receive(sc);
         if (message instanceof Hungry) {
            hungry = true;
            if (!haveR) rightServantChannel.send(new NeedL());
            else System.out.println
               (age() + " hungry philosopher " + id + " has right fork");
            if (!haveL) leftServantChannel.send(new NeedR());
            else System.out.println
               (age() + " hungry philosopher " + id + " has left fork");
            while (!(haveR && haveL)) {  // while hungry, wait for forks
               sc = new ServantCondition(hungry, dirtyL, dirtyR);
               message = myChannel.receive(sc);
               if (message instanceof PassL) {  // left servant sends fork
// Move the fork from where it was to be next to this philosopher and then
// change its symbol to be light gray, i.e., not dirty.
                  xa.moveTo(forkL, holderL);
                  xa.color(forkL, Color.lightGray);
                  xa.fill("phil"+id, xa.HALF);
                  haveL = true; dirtyL = false;
                  System.out.println
                     (age() + " hungry philosopher " + id + " got left fork");
               } else if (message instanceof PassR) {
                  // right servant sends fork
// Ditto.
                  xa.moveTo(forkR, holderR);
                  xa.color(forkR, Color.lightGray);
                  xa.fill("phil"+id, xa.HALF);
                  haveR = true; dirtyR = false;
                  System.out.println
                     (age() + " hungry philosopher " + id + " got right fork");
               } else if (message instanceof NeedL) {  // dirtyL is true
                  // hungry philosopher must relinquish dirty fork
                  // to avoid starvation
                  haveL = false; dirtyL = false;
                  leftServantChannel.send(new PassR());
                  leftServantChannel.send(new NeedR());
                  xa.color("phil"+id, Color.red);
                  System.out.println(age() + " hungry philosopher "
                     + id + " sends dirty left fork");
               } else if (message instanceof NeedR) {  // dirtyR is true
                  // hungry philosopher must relinquish dirty fork
                  // to avoid starvation
                  haveR = false; dirtyR = false;
                  rightServantChannel.send(new PassL());
                  rightServantChannel.send(new NeedL());
                  xa.color("phil"+id, Color.red);
                  System.out.println(age() + " hungry philosopher "
                     + id + " sends dirty right fork");
               } else System.err.println
                  ("Servant" + id + " received bogus message!");
            }
            System.out.println
               (age() + " philosopher " + id + " has both forks");
            V(eat); dirtyR = true; dirtyL = true;
// Now that the philosopher is eating, its forks are getting dirty so
// change their symbols to be yellow.
            xa.color(forkL, Color.yellow);
            xa.color(forkR, Color.yellow);
            P(releaseForks);
            hungry = false;
// Now that the philosopher has finished eating, its forks are dirty so
// change their symbols to be orange.
            xa.color(forkL, Color.orange);
            xa.color(forkR, Color.orange);
            System.out.println
               (age() + " philosopher " + id + " is finished eating");
         } else if (message instanceof NeedR) {
            // not hungry and have right fork
            if (!haveR) System.err.println
               ("Servant " + id + " asked for right fork it does not have!");
            haveR = false; dirtyR = false; rightServantChannel.send(new PassL());
            System.out.println
               (age() + " unhungry philosopher " + id + " sends right fork");
         } else if (message instanceof NeedL) {
            // not hungry and have left fork
            if (!haveL) System.err.println
               ("Servant " + id + " asked for left fork it does not have!");
            haveL = false; dirtyL = false; leftServantChannel.send(new PassR());
            System.out.println
               (age() + " unhungry philosopher " + id + " sends left fork");
         } else
            System.err.println("Servant" + id + " received bogus message!");
      }
   }
}

public class AnimatedDistributedPhilosophers extends MyObject {

   private static final double TWO_PI = 2.0 * Math.PI;
   private static XtangoApplet xa = null;                // animation...

   public AnimatedDistributedPhilosophers(XtangoApplet a) { xa = a; }

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

      // process non-option command line arguments
      int[] napThink = new int[numPhilosophers];
      int[] napEat = new int[numPhilosophers];
      int argNum = go.optIndexGet();
      for (int i = 0; i < numPhilosophers; i++) {
         napThink[i] = 8; napEat[i] = 2;  // defaults
         napThink[i] = go.tryArg(argNum++, napThink[i]);
         napEat[i] = go.tryArg(argNum++, napEat[i]);
      }

      xa.begin();
// Change coordinates so 0,0 is the center, then create a big black outline
// circle to be the table.
      xa.coords(-1.5f, -1.5f, 1.5f, 1.5f);
      xa.circle("C0", 0.0f, 0.0f, (float)1, Color.black, xa.OUTLINE);
      // bowl of spaghetti
      xa.circle("Cs", 0.0f, 0.0f, 0.5f, Color.orange, xa.HALF);
// Put some annotated symbols on the screen.
      xa.circle("cf", -1.4f, -0.6f, 0.02f, Color.lightGray, xa.SOLID);
      xa.text("cft", -1.3f, -0.625f, false, Color.black, "clean fork");
      xa.circle("uf", -1.4f, -0.7f, 0.02f, Color.yellow, xa.SOLID);
      xa.text("uft", -1.3f, -0.725f, false, Color.black, "in-use fork");
      xa.circle("df", -1.4f, -0.8f, 0.02f, Color.orange, xa.SOLID);
      xa.text("dft", -1.3f, -0.825f, false, Color.black, "dirty fork");
      xa.circle("C1", -1.4f, -0.9f, 0.05f, Color.black, xa.OUTLINE);
      xa.text("T1", -1.3f, -0.925f, false, Color.black, "THINKING");
      xa.circle("C2", -1.4f, (float)-1, 0.05f, Color.green, xa.SOLID);
      xa.text("T2", -1.3f, -1.025f, false, Color.black, "HUNGRY");
      xa.circle("C3", -1.4f, -1.1f, 0.05f, Color.blue, xa.SOLID);
      xa.text("T3", -1.3f, -1.125f, false, Color.black, "EATING");
      xa.circle("C4", -1.4f, -1.2f, 0.05f, Color.red, xa.SOLID);
      xa.text("T4", -1.3f, -1.225f, false, Color.black,
         "HUNGRY and had to give up a dirty fork");
      xa.circle("C5", -1.4f, -1.3f, 0.05f, Color.red, xa.HALF);
      xa.circle("C6", -1.3f, -1.3f, 0.05f, Color.green, xa.HALF);
      xa.text("T6", -1.2f, -1.325f, false, Color.black,
         "HUNGRY and received a fork");
// Seat the philosophers around the table.  Philosopher 1 is to the left of
// philosopher 0 and fork 0 is to the left of philosopher 0.  Put a clean
// fork between each philosopher.
      float gap = (float) TWO_PI/numPhilosophers;
      for (int i = 0; i < numPhilosophers; i++) {
         double radianp = i*gap;
         float sinp = (float) Math.sin(radianp);
         float cosp = (float) Math.cos(radianp);
         xa.circle("phil"+i, sinp, cosp, 0.2f*gap, Color.black, xa.OUTLINE);
         xa.bigText("TP"+i, sinp*0.55f, cosp*0.55f, true, Color.black, ""+i);
         double radianf = radianp + 0.5f*gap;
         float sinf = (float) Math.sin(radianf);
         float cosf = (float) Math.cos(radianf);
         xa.pointLine("fork"+i, sinf, cosf, 0.4f*sinf, 0.4f*cosf,
            Color.lightGray, xa.THICK);
// Put nearly invisible circles (points) on the left and right side of each
// philosopher to be places the forks can be moved to when the philosopher
// gets possession of a fork.
         double radianLf = radianp + 0.2f*gap;
         double radianRf = radianp - 0.2f*gap;
         float sinLf = (float) Math.sin(radianLf);
         float cosLf = (float) Math.cos(radianLf);
         float sinRf = (float) Math.sin(radianRf);
         float cosRf = (float) Math.cos(radianRf);
         xa.circle("forkL"+i, sinLf, cosLf, 0.001f, Color.black, xa.OUTLINE);
         xa.circle("forkR"+i, sinRf, cosRf, 0.001f, Color.black, xa.OUTLINE);
      }
      xa.delay(10);                             // ...animation

      // create the communication channels
      AsyncConditionalMessagePassing[] channel
         = new AsyncConditionalMessagePassing[numPhilosophers];
      for (int i = 0; i < numPhilosophers; i++) {
         channel[i] = new AsyncConditionalMessagePassing(false);
      }

      // set up the initial fork locations; this is not symmetric
      // to avoid deadlock; all philosophers have their left fork
      // clean except the last one who has no forks; the first
      // philosopher has both forks dirty
      boolean[] haveL = new boolean[numPhilosophers];
      boolean[] haveR = new boolean[numPhilosophers];
      boolean[] dirtyL = new boolean[numPhilosophers];
      boolean[] dirtyR = new boolean[numPhilosophers];
      String[] forkL = new String[numPhilosophers];
      String[] forkR = new String[numPhilosophers];
      String[] holderL = new String[numPhilosophers];
      String[] holderR = new String[numPhilosophers];
      for (int i = 0; i < numPhilosophers; i++) {
         forkL[i] = "fork" + i;
         forkR[i] = "fork" + ((i-1+numPhilosophers)%numPhilosophers);
         holderL[i] = "forkL" + i;  holderR[i] = "forkR" + i;
         haveL[i] = true;
         haveR[i] = dirtyL[i] = dirtyR[i] = false;
      }
      haveL[0] = haveR[0] = dirtyL[0] = dirtyR[0] = true;
      haveL[numPhilosophers-1] = false;

      // create the Servants with self-starting threads
      Servant[] servant = new Servant[numPhilosophers];
      for (int i = 0; i < numPhilosophers; i++) {
         servant[i] = new Servant("Servant", i,
            forkL[i], forkR[i], holderL[i], holderR[i],
            haveL[i], dirtyL[i], haveR[i], dirtyR[i],
            channel[i],                           // this Servant's channel
            channel[(i+1)%numPhilosophers],       // left neighbor's channel
            channel[(i-1+numPhilosophers)%numPhilosophers],
                                               // right neighbor's channel
            xa);
      }
      xa.delay(10);

      // create the Philosophers with self-starting threads
      DistributedPhilosopher[] phil =
         new DistributedPhilosopher[numPhilosophers];
      for (int i = 0; i < numPhilosophers; i++)
         phil[i] = new DistributedPhilosopher("Philosopher", i,
            napThink[i]*1000, napEat[i]*1000, servant[i]);
      System.out.println("All Philosopher and Servant threads started");

      // let the Philosophers and Servants run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the Philosophers and Servants and exit");
      for (int i = 0; i < numPhilosophers; i++) phil[i].stop();
      xa.end();
   }
}
