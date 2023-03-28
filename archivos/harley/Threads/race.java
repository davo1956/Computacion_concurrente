import Utilities.*;

class Racer extends MyObject implements Runnable {

   private int M = 0;              // these fields are shared by both
   private volatile long sum = 0;  // threads since there is one object

   public Racer(String name, int M) {
      super(name);
      this.M = M;
      System.out.println("age()=" + age() + ", "
         + getName() + " is alive, M=" + M);
   }

   private long fn(long j, int k) {
      long total = j;
      for (int i = 1;  i <= k; i++) total += i;
      return total;
   }

   public void run() {
      System.out.println("age()=" + age() + ", "
         + getThreadName() + " is running");
      for (int m = 1; m <= M; m++)
/*
 * "N = N + 1" type lost update (race condition) in following line
 */
         sum = fn(sum, m);

      System.out.println("age()=" + age() + ", "
         + getThreadName() + " is done, sum = " + sum);
   }
}

class RaceTwoThreads extends MyObject {

   private static int M = 10;
   private final static int numRacers = 2;

   public static void main(String[] args) {

      // parse command line arguments, if any, to override defaults
      GetOpt go = new GetOpt(args, "UtSM:");
      go.optErr = true;
      String usage = "Usage: -t -S -M m";
      int ch = -1;
      boolean timeSlicingEnsured = false;
      boolean forceSequential = false;  // run the threads consecutively
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 't') timeSlicingEnsured = true;
         else if ((char)ch == 'M')
            M = go.processArg(go.optArgGet(), M);
         else if ((char)ch == 'S') forceSequential = true;
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("RaceTwoThreads: M=" + M + ", timeSlicingEnsured="
         + timeSlicingEnsured + " forceSequential=" + forceSequential);

      // enable time slicing Solaris (50 msec); noop on Windows 95
      if (timeSlicingEnsured) ensureTimeSlicing(50); // so threads share CPU

      // start the two threads, both in the same object
      // so they share one instance of its variable sum
      Racer racerObject = new Racer("RacerObject", M);
      Thread[] racer = new Thread[numRacers];
      for (int i = 0; i < numRacers; i++)
         racer[i] = new Thread(racerObject, "RacerThread" + i);
      for (int i = 0; i < numRacers; i++) {
         racer[i].start();
         if (forceSequential)
            try {
               racer[i].join();  // wait for it to terminate
            } catch (InterruptedException e) {
               System.err.println("interrupted out of join");
            }     
      }
      System.out.println("age()=" + age() + ", all Racer threads started");

      // wait for them to finish if not forced consecutive
      if (!forceSequential)
         try {
            for (int i = 0; i < numRacers; i++) racer[i].join();
         } catch (InterruptedException e) {
            System.err.println("interrupted out of join");
         }

      // correct race-free final value of sum is 2*220 = 440 for M of 10
      // and 2*1335334000 = 2670668000 for M of 2000 (so `long sum' needed)
      System.out.println("RaceTwoThreads done");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac race.java

D:\>java RaceTwoThreads
Java version=1.1.1, Java vendor=Sun Microsystems Inc.
OS name=Windows 95, OS arch=x86, OS version=4.0
Wed Jul 02 16:59:16 EDT 1997
RaceTwoThreads: M=10, timeSlicingEnsured=false forceSequential=false
age()=60, RacerObject is alive, M=10
age()=60, all Racer threads started
age()=60, RacerThread0 is running
age()=60, RacerThread0 is done, sum = 220
age()=110, RacerThread1 is running
age()=110, RacerThread1 is done, sum = 440
RaceTwoThreads done

D:\>java RaceTwoThreads -M2000
RaceTwoThreads: M=2000, timeSlicingEnsured=false forceSequential=false
age()=50, RacerObject is alive, M=2000
age()=50, all Racer threads started
age()=50, RacerThread0 is running
age()=110, RacerThread1 is running
age()=2080, RacerThread0 is done, sum = 1335334000
age()=2080, RacerThread1 is done, sum = 1335345480
RaceTwoThreads done

D:\>java RaceTwoThreads -M2000 -S
RaceTwoThreads: M=2000, timeSlicingEnsured=false forceSequential=true
age()=0, RacerObject is alive, M=2000
age()=60, RacerThread0 is running
age()=1320, RacerThread0 is done, sum = 1335334000
age()=1380, RacerThread1 is running
age()=2360, RacerThread1 is done, sum = 2670668000
age()=2360, all Racer threads started
RaceTwoThreads done

% javac race.java

% java RaceTwoThreads -M2000
Java version=1.1_Final, Java vendor=Sun Microsystems Inc.
OS name=Solaris, OS arch=sparc, OS version=2.x
Tue Jul 01 13:56:15 PDT 1997
RaceTwoThreads: M=2000, timeSlicingEnsured=false forceSequential=false
age()=11, RacerObject is alive, M=2000
age()=14, all Racer threads started
age()=15, RacerThread0 is running
age()=1706, RacerThread0 is done, sum = 1335334000
age()=1708, RacerThread1 is running
age()=3409, RacerThread1 is done, sum = 2670668000

% java RaceTwoThreads -M2000 -t
RaceTwoThreads: M=2000, timeSlicingEnsured=true forceSequential=false
Scheduler: timeSlice=50 randomSlice=false priority=10
age()=35, RacerObject is alive, M=2000
age()=37, all Racer threads started
age()=38, RacerThread0 is running
age()=84, RacerThread1 is running
age()=3421, RacerThread0 is done, sum = 1335334000
age()=3456, RacerThread1 is done, sum = 1341543895
RaceTwoThreads done
                                            ... end of example run(s)  */
