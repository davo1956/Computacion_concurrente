import Utilities.*;
import Synchronization.*;

// message types
class Hungry {} // philosopher sends this to its servant
class NeedL {}  // servants
class NeedR {}  // send
class PassL {}  // these to
class PassR {}  // each other

class Philosopher extends MyObject implements Runnable {

   private int id = 0;
   private int napThink = 0; // both are in
   private int napEat = 0;   // milliseconds
   private Servant myServant = null;

   public Philosopher(String name, int id, int napThink,
         int napEat, Servant myServant) {
      super(name + " " + id);
      this.id = id;
      this.napThink = napThink;
      this.napEat = napEat;
      this.myServant = myServant;
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
         myServant.takeForks(id);
         eat();
         myServant.putForks(id);
      }
   }
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

   private BinarySemaphore eat = new BinarySemaphore(0);
   private BinarySemaphore releaseForks = new BinarySemaphore(0);

   public Servant(String name, int id,
         boolean haveL, boolean dirtyL, boolean haveR, boolean dirtyR,
         AsyncConditionalMessagePassing myChannel,
         AsyncConditionalMessagePassing leftServantChannel,
         AsyncConditionalMessagePassing rightServantChannel) {
      super(name + " " + id);
      this.id = id;
      this.haveL = haveL; this.dirtyL = dirtyL;
      this.haveR = haveR; this.dirtyR = dirtyR;
      this.myChannel = myChannel;
      this.leftServantChannel = leftServantChannel;
      this.rightServantChannel = rightServantChannel;
      System.out.println("Servant " + getName() + " is alive:\n"
         + "haveL=" + haveL + ", haveR=" + haveR
         + ", dirtyL=" + dirtyL + ", dirtyR=" + dirtyR);
      new Thread(this).start();
   }

   public void takeForks(int id) {
      myChannel.send(new Hungry());  // non blocking
      P(eat);                  // wait for empty message
   }

   public void putForks(int id) {
      V(releaseForks);         // send empty message
   }

   public void run() {
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
                  haveL = true; dirtyL = false;
                  System.out.println
                     (age() + " hungry philosopher " + id + " got left fork");
               } else if (message instanceof PassR) {
                  // right servant sends fork
                  haveR = true; dirtyR = false;
                  System.out.println
                     (age() + " hungry philosopher " + id + " got right fork");
               } else if (message instanceof NeedL) {  // dirtyL is true
                  // hungry philosopher must relinquish dirty fork
                  // to avoid starvation
                  haveL = false; dirtyL = false;
                  leftServantChannel.send(new PassR());
                  leftServantChannel.send(new NeedR());
                  System.out.println(age() + " hungry philosopher "
                     + id + " sends dirty left fork");
               } else if (message instanceof NeedR) {  // dirtyR is true
                  // hungry philosopher must relinquish dirty fork
                  // to avoid starvation
                  haveR = false; dirtyR = false;
                  rightServantChannel.send(new PassL());
                  rightServantChannel.send(new NeedL());
                  System.out.println(age() + " hungry philosopher "
                     + id + " sends dirty right fork");
               } else System.err.println
                  ("Servant" + id + " received bogus message!");
            }
            System.out.println
               (age() + " philosopher " + id + " has both forks");
            V(eat); dirtyR = true; dirtyL = true;
            P(releaseForks);
            hungry = false;
            System.out.println
               (age() + " philosopher " + id + " is finished eating");
         } else if (message instanceof NeedR) {
            // not hungry and have right fork
            if (!haveR) System.err.println
/*
 *   Since haveR cannot be false here, this println will never be executed.
 * The only way a NeedR message could be sent to a philosopher not having
 * the fork (false haveR) is if the philosopher's right neighbor was forced
 * to give up a dirty fork, and it sent the fork, then followed that message
 * with a needR message, but the needR message got here first.  In this
 * situation, the philosopher to whom the PassR and overtaking needR were
 * sent would be hungry.  But we are in a non-hungry part of the code.  So
 * the right neighbor was not asked for its left fork, and it didn't send
 * any PassR and NeedR messages that arrived out of order.
 *   Even if the philosopher were hungry, the needR message would remain
 * queued because checkCondition() on it would be false.  This is because
 * hungry is true and dirtyR false in this situation.
 */
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
      int argNum = go.optIndexGet();
      for (int i = 0; i < numPhilosophers; i++) {
         napThink[i] = 8; napEat[i] = 2;  // defaults
         napThink[i] = go.tryArg(argNum++, napThink[i]);
         napEat[i] = go.tryArg(argNum++, napEat[i]);
      }

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
      for (int i = 0; i < numPhilosophers; i++) {
         haveL[i] = true;
         haveR[i] = dirtyL[i] = dirtyR[i] = false;
      }
      haveL[0] = haveR[0] = dirtyL[0] = dirtyR[0] = true;
      haveL[numPhilosophers-1] = false;

      // create the Servants with self-starting threads
      Servant[] servant = new Servant[numPhilosophers];
      for (int i = 0; i < numPhilosophers; i++) {
         servant[i] = new Servant("Servant", i,
            haveL[i], dirtyL[i], haveR[i], dirtyR[i],
            channel[i],                           // this Servant's channel
            channel[(i+1)%numPhilosophers],       // left neighbor's channel
            channel[(i-1+numPhilosophers)%numPhilosophers]);
                                               // right neighbor's channel
      }

      // create the Philosophers with self-starting threads
      for (int i = 0; i < numPhilosophers; i++) {
         new Philosopher("Philosopher", i,
            napThink[i]*1000, napEat[i]*1000, servant[i]);
      }
      System.out.println("All Philosopher and Servant threads started");

      // let the Philosophers and Servants run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the Philosophers and Servants and exit");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac ddph.java

D:\>java DiningPhilosophers -R10 4 2 4 2 4 2 4 2 4 2
DiningPhilosophers: numPhilosophers=5, runTime=10
Servant Servant 0 is alive:
haveL=true, haveR=true, dirtyL=true, dirtyR=true
Servant Servant 1 is alive:
haveL=true, haveR=false, dirtyL=false, dirtyR=false
Servant Servant 2 is alive:
haveL=true, haveR=false, dirtyL=false, dirtyR=false
Servant Servant 3 is alive:
haveL=true, haveR=false, dirtyL=false, dirtyR=false
Servant Servant 4 is alive:
haveL=false, haveR=false, dirtyL=false, dirtyR=false
Philosopher 0 is alive, napThink=4000, napEat=2000
Philosopher 1 is alive, napThink=4000, napEat=2000
Philosopher 2 is alive, napThink=4000, napEat=2000
age()=170, Philosopher 0 is thinking for 1487 ms
age()=170, Philosopher 1 is thinking for 1027 ms
Philosopher 3 is alive, napThink=4000, napEat=2000
Philosopher 4 is alive, napThink=4000, napEat=2000
All Philosopher and Servant threads started
age()=170, Philosopher 2 is thinking for 2160 ms
age()=170, Philosopher 3 is thinking for 2323 ms
age()=170, Philosopher 4 is thinking for 52 ms
age()=220, Philosopher 4 wants to eat
330 unhungry philosopher 3 sends left fork
330 unhungry philosopher 0 sends right fork
390 hungry philosopher 4 got right fork
440 hungry philosopher 4 got left fork
440 philosopher 4 has both forks
age()=440, Philosopher 4 is eating for 1842 ms
age()=1210, Philosopher 1 wants to eat
1210 hungry philosopher 1 has left fork
1210 unhungry philosopher 0 sends left fork
1210 hungry philosopher 1 got right fork
1210 philosopher 1 has both forks
age()=1210, Philosopher 1 is eating for 255 ms
age()=1480, Philosopher 1 is thinking for 3458 ms
1480 philosopher 1 is finished eating
age()=1650, Philosopher 0 wants to eat
1650 unhungry philosopher 1 sends right fork
1650 hungry philosopher 0 got left fork
age()=2310, Philosopher 4 is thinking for 3947 ms
2310 philosopher 4 is finished eating
2310 unhungry philosopher 4 sends left fork
2310 hungry philosopher 0 got right fork
2310 philosopher 0 has both forks
age()=2310, Philosopher 0 is eating for 287 ms
age()=2360, Philosopher 2 wants to eat
2360 hungry philosopher 2 has left fork
2360 unhungry philosopher 1 sends left fork
2360 hungry philosopher 2 got right fork
2360 philosopher 2 has both forks
age()=2360, Philosopher 2 is eating for 1157 ms
age()=2530, Philosopher 3 wants to eat
2530 unhungry philosopher 4 sends right fork
2530 hungry philosopher 3 got left fork
age()=2580, Philosopher 0 is thinking for 3759 ms
2580 philosopher 0 is finished eating
age()=3520, Philosopher 2 is thinking for 2141 ms
3520 philosopher 2 is finished eating
3520 unhungry philosopher 2 sends left fork
3520 hungry philosopher 3 got right fork
3520 philosopher 3 has both forks
age()=3570, Philosopher 3 is eating for 953 ms
age()=4510, Philosopher 3 is thinking for 2085 ms
4510 philosopher 3 is finished eating
age()=4940, Philosopher 1 wants to eat
4940 unhungry philosopher 0 sends left fork
4940 unhungry philosopher 2 sends right fork
4940 hungry philosopher 1 got right fork
4940 hungry philosopher 1 got left fork
4940 philosopher 1 has both forks
age()=4940, Philosopher 1 is eating for 618 ms
age()=5550, Philosopher 1 is thinking for 533 ms
5550 philosopher 1 is finished eating
age()=5660, Philosopher 2 wants to eat
5660 unhungry philosopher 1 sends left fork
5660 unhungry philosopher 3 sends right fork
5660 hungry philosopher 2 got right fork
5710 hungry philosopher 2 got left fork
5710 philosopher 2 has both forks
age()=5710, Philosopher 2 is eating for 48 ms
age()=5770, Philosopher 2 is thinking for 946 ms
5770 philosopher 2 is finished eating
age()=6100, Philosopher 1 wants to eat
6100 hungry philosopher 1 has right fork
6100 unhungry philosopher 2 sends right fork
6100 hungry philosopher 1 got left fork
6100 philosopher 1 has both forks
age()=6100, Philosopher 1 is eating for 998 ms
age()=6260, Philosopher 4 wants to eat
6260 unhungry philosopher 3 sends left fork
6260 unhungry philosopher 0 sends right fork
6260 hungry philosopher 4 got right fork
6260 hungry philosopher 4 got left fork
6260 philosopher 4 has both forks
age()=6260, Philosopher 4 is eating for 251 ms
age()=6370, Philosopher 0 wants to eat
age()=6540, Philosopher 4 is thinking for 463 ms
6540 philosopher 4 is finished eating
6540 unhungry philosopher 4 sends left fork
6540 hungry philosopher 0 got right fork
age()=6590, Philosopher 3 wants to eat
6590 unhungry philosopher 2 sends left fork
6590 unhungry philosopher 4 sends right fork
6590 hungry philosopher 3 got right fork
6590 hungry philosopher 3 got left fork
6590 philosopher 3 has both forks
age()=6590, Philosopher 3 is eating for 1459 ms
age()=6700, Philosopher 2 wants to eat
age()=6980, Philosopher 4 wants to eat
age()=7140, Philosopher 1 is thinking for 721 ms
7140 philosopher 1 is finished eating
7140 unhungry philosopher 1 sends right fork
7140 hungry philosopher 0 got left fork
7140 philosopher 0 has both forks
age()=7140, Philosopher 0 is eating for 269 ms
7140 unhungry philosopher 1 sends left fork
7140 hungry philosopher 2 got right fork
age()=7420, Philosopher 0 is thinking for 2709 ms
7420 philosopher 0 is finished eating
7420 unhungry philosopher 0 sends right fork
7420 hungry philosopher 4 got left fork
age()=7860, Philosopher 1 wants to eat
7860 unhungry philosopher 0 sends left fork
7860 hungry philosopher 1 got right fork
age()=8080, Philosopher 3 is thinking for 3685 ms
8080 philosopher 3 is finished eating
8080 unhungry philosopher 3 sends right fork
8080 hungry philosopher 2 got left fork
8080 philosopher 2 has both forks
age()=8080, Philosopher 2 is eating for 1034 ms
8080 unhungry philosopher 3 sends left fork
8080 hungry philosopher 4 got right fork
8080 philosopher 4 has both forks
age()=8080, Philosopher 4 is eating for 1237 ms
age()=9120, Philosopher 2 is thinking for 3047 ms
9120 philosopher 2 is finished eating
9120 unhungry philosopher 2 sends right fork
9120 hungry philosopher 1 got left fork
9120 philosopher 1 has both forks
age()=9170, Philosopher 1 is eating for 1831 ms
age()=9340, Philosopher 4 is thinking for 1571 ms
9340 philosopher 4 is finished eating
age()=10110, Philosopher 0 wants to eat
10110 unhungry philosopher 4 sends left fork
10110 hungry philosopher 0 got right fork
age()=10160, time to stop the Philosophers and Servants and exit
                                            ... end of example run(s)  */
