import Utilities.*;

class Racer extends MyObject implements Runnable {

   private int M = 0;
   private long sum = 0;  // `volatile' no longer needed
   private Object mutex = null; // implicit binary semaphore

   public Racer(String name, int M) {
      super(name);
      this.M = M;
      // use `this' object for synchronization since both threads execute in
      mutex = this;  // a single object; also can use mutex = new Object();
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
         synchronized (mutex) { // implied P(mutex)
            sum = fn(sum, m);
         }                      // implied V(mutex)

      System.out.println("age()=" + age() + ", "
         + getThreadName() + " is done, sum = " + sum);
   }
}

class RaceTwoThreads extends MyObject {

   private static int M = 10;
   private final static int numRacers = 2;

   public static void main(String[] args) {

      // parse command line arguments, if any, to override defaults
      GetOpt go = new GetOpt(args, "UtM:");
      go.optErr = true;
      String usage = "Usage: -t -M m";
      int ch = -1;
      boolean timeSlicingEnsured = false;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 't') timeSlicingEnsured = true;
         else if ((char)ch == 'M')
            M = go.processArg(go.optArgGet(), M);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("RaceTwoThreads: M=" + M
         + ", timeSlicingEnsured=" + timeSlicingEnsured);

      // enable time slicing Solaris (50 msec); noop on Windows 95
      if (timeSlicingEnsured) ensureTimeSlicing(50); // so threads share CPU

      // start the two threads, both in the same object
      // so they share one instance of its variable sum
      Racer racerObject = new Racer("RacerObject", M);
      Thread[] racer = new Thread[numRacers];
      for (int i = 0; i < numRacers; i++)
         racer[i] = new Thread(racerObject, "RacerThread" + i);
      for (int i = 0; i < numRacers; i++) racer[i].start();
      System.out.println("age()=" + age() +
         ", all Racer threads started");

      // wait for them to finish
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

D:\>javac norc.java

D:\>java RaceTwoThreads
Java version=1.1.1, Java vendor=Sun Microsystems Inc.
OS name=Windows 95, OS arch=x86, OS version=4.0
Wed Jul 02 17:01:15 EDT 1997
RaceTwoThreads: M=10, timeSlicingEnsured=false
age()=60, RacerObject is alive, M=10
age()=60, all Racer threads started
age()=60, RacerThread0 is running
age()=60, RacerThread0 is done, sum = 220
age()=60, RacerThread1 is running
age()=60, RacerThread1 is done, sum = 440
RaceTwoThreads done

D:\>java RaceTwoThreads -M2000
RaceTwoThreads: M=2000, timeSlicingEnsured=false
age()=60, RacerObject is alive, M=2000
age()=60, all Racer threads started
age()=60, RacerThread0 is running
age()=60, RacerThread1 is running
age()=2370, RacerThread1 is done, sum = 2540975095
age()=2420, RacerThread0 is done, sum = 2670668000
RaceTwoThreads done

% javac norc.java

% java RaceTwoThreads -M2000 -t
Java version=1.1_Final, Java vendor=Sun Microsystems Inc.
OS name=Solaris, OS arch=sparc, OS version=2.x
Tue Jul 01 14:00:42 PDT 1997
RaceTwoThreads: M=2000, timeSlicingEnsured=true
Scheduler: timeSlice=50 randomSlice=false priority=10
age()=19, RacerObject is alive, M=2000
age()=21, all Racer threads started
age()=22, RacerThread0 is running
age()=68, RacerThread1 is running
age()=2992, RacerThread0 is done, sum = 2093590456
age()=3473, RacerThread1 is done, sum = 2670668000
RaceTwoThreads done
                                            ... end of example run(s)  */
