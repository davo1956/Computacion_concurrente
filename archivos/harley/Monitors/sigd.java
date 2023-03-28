/*
 * This example shows that Java wait/notify use the `signal and continue'
 * signaling discipline.  One may generate multiple signals inside a
 * monitor and continue executing inside the monitor until a return.
 * IMPORTANT NOTE: it is possible that a signaled thread may not resume
 * inside the monitor until after another thread that was blocked on a
 * method call enters and leaves (or waits inside) the monitor.  This
 * can be seen happening in the sample output shown below.  Sleeper 3
 * gets in the monitor before Sleepers 1 and 2 proceed after being
 * signaled.  This is an example of "barging."  Caveat programmer!
 *
 * Also note that the Windows 95 sample run shows the Sleeper threads
 * waking up after being signaled and proceeding in the monitor in the
 * opposite order that they queued up on the condition variable.  In
 * contrast, the Solaris sample run shows FIFO queueing and wakeup on
 * the condition variable.  But FIFO is not guaranteed!
 */
import Utilities.*;

class DriverShowSigDis extends MyObject {

   private static Thread[] sleeperThread = null;
   private static Thread wakerThread = null;
   private static MonitorShowSigDis mssd = null;

   public static void main(String[] arg) {
      mssd = new MonitorShowSigDis();
      sleeperThread = new Thread[3];
      for (int i = 0; i < 3; i++)
         sleeperThread[i] = new Thread(new SleeperThread(i+1, mssd));
      wakerThread = new Thread(new WakerThread(9, mssd));
      for (int i = 0; i < 3; i++) sleeperThread[i].start();
      wakerThread.start();
      nap(10000);
      System.exit(0);
   }

}

/* ............... Example compile and run(s)

% javac sigd.java

% java DriverShowSigDis
Sleeper 1 wants to enter monitor gotoSleep method at time 1051
Thread 1 enters monitor gotoSleep method at time 1087
Thread 1 queues on c.v. gotoSleep method at time 1200
Sleeper 2 wants to enter monitor gotoSleep method at time 2049
Thread 2 enters monitor gotoSleep method at time 2058
Thread 2 queues on c.v. gotoSleep method at time 2169
Waker 9 wants to enter monitor wakeUp method at time 2559
Thread 9 enters monitor wakeUp method at time 2570
Sleeper 3 wants to enter monitor gotoSleep method at time 3059
Thread 9 signals c.v in wakeUp method at time 3580
Thread 9 continues  in  wakeUp method at time 3590
Thread 9 signal two! in wakeUp method at time 3700
Thread 9 leaves monitor wakeUp method at time 3819
Waker 9 done at time 3828
Thread 3 enters monitor gotoSleep method at time 3837
Thread 3 queues on c.v. gotoSleep method at time 3949
Thread 1 awakes on c.v. gotoSleep method at time 3958
Thread 1 leaves monitor gotoSleep method at time 4070
Sleeper 1 done at time 4081
Thread 2 awakes on c.v. gotoSleep method at time 4089
Thread 2 leaves monitor gotoSleep method at time 4199
Sleeper 2 done at time 4207

D:\>javac sigd.java

D:\>java DriverShowSigDis
Sleeper 1 wants to enter monitor gotoSleep method at time 1050
Thread 1 enters monitor gotoSleep method at time 1050
Thread 1 queues on c.v. gotoSleep method at time 1160
Sleeper 2 wants to enter monitor gotoSleep method at time 2040
Thread 2 enters monitor gotoSleep method at time 2040
Thread 2 queues on c.v. gotoSleep method at time 2150
Waker 9 wants to enter monitor wakeUp method at time 2530
Thread 9 enters monitor wakeUp method at time 2530
Sleeper 3 wants to enter monitor gotoSleep method at time 3030
Thread 9 signals c.v in wakeUp method at time 3570
Thread 9 continues  in  wakeUp method at time 3570
Thread 9 signal two! in wakeUp method at time 3680
Thread 9 leaves monitor wakeUp method at time 3790
Waker 9 done at time 3790
Thread 3 enters monitor gotoSleep method at time 3790
Thread 3 queues on c.v. gotoSleep method at time 3900
Thread 2 awakes on c.v. gotoSleep method at time 3900
Thread 2 leaves monitor gotoSleep method at time 4010
Sleeper 2 done at time 4010
Thread 1 awakes on c.v. gotoSleep method at time 4010
Thread 1 leaves monitor gotoSleep method at time 4120
Sleeper 1 done at time 4120
                                            ... end of example run(s)  */

class MonitorShowSigDis extends MyObject {

   public MonitorShowSigDis() {}

   public synchronized void gotoSleep(int id) {
      System.out.println
         ("Thread "+ id +" enters monitor gotoSleep method at time "+ age());
      nap(100);
      System.out.println
         ("Thread "+ id +" queues on c.v. gotoSleep method at time "+ age());
      try {
         wait();
      } catch (InterruptedException e) {
         System.err.println("interrupted out of wait");
      }
      System.out.println
         ("Thread "+ id +" awakes on c.v. gotoSleep method at time "+ age());
      nap(100);
      System.out.println
         ("Thread "+ id +" leaves monitor gotoSleep method at time "+ age());
   }

   public synchronized void wakeUp(int id) {
      System.out.println
         ("Thread "+ id +" enters monitor wakeUp method at time "+ age());
      nap(1000);
      System.out.println
         ("Thread "+ id +" signals c.v in wakeUp method at time "+ age());
      notify();
      System.out.println
         ("Thread "+ id +" continues  in  wakeUp method at time "+ age());
      nap(100);
      System.out.println
         ("Thread "+ id +" signal two! in wakeUp method at time "+ age());
      notify();
      nap(100);
      System.out.println
         ("Thread "+ id +" leaves monitor wakeUp method at time "+ age());
   }

}

class SleeperThread extends MyObject implements Runnable {

   private int id = 0;
   private MonitorShowSigDis mssd = null;

   public SleeperThread(int id, MonitorShowSigDis mssd) {
      this.id = id;
      this.mssd = mssd;
   }

   public void run() {
      nap(id*1000);
      System.out.println("Sleeper "+ id
         +" wants to enter monitor gotoSleep method at time "+ age());
      mssd.gotoSleep(id);
      System.out.println
         ("Sleeper "+ id +" done at time "+ age());
   }
}

class WakerThread extends MyObject implements Runnable {

   private int id = 0;
   private MonitorShowSigDis mssd = null;

   public WakerThread(int id, MonitorShowSigDis mssd) {
      this.id = id;
      this.mssd = mssd;
   }

   public void run() {
      nap(2500);
      System.out.println
         ("Waker 9 wants to enter monitor wakeUp method at time "+ age());
      mssd.wakeUp(9);
      System.out.println
         ("Waker 9 done at time "+ age());
   }
}
