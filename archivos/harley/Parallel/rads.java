/*
 * Parallel Radix sort.
 * The N numbers are sent to N workers.  Then each worker broadcasts its
 * number to all other workers.  Each worker records how many numbers it
 * sees less than its own.  Then each worker sends its count and number
 * to the driver, who puts it where it belongs in the sorted order, which
 * is the value of the counter in which it recorded the number of
 * less-than-its-own numbers it saw.
 */

import Utilities.*;
import Synchronization.*;

class Result { public int number, count;
   public Result(int n, int c) { number = n; count = c; }
}

class RadixSort extends MyObject implements Runnable {

   private static final int MAX_PER_LINE = 15;
   private static int N = 15;
   private static int RANGE = 1000;
   private static boolean debug = false;
   private static int[] nums = null;
   private static MessagePassing[] channel = null;
   private static MessagePassing reply = null;

   private int id = -1, mine = 0;

   private RadixSort(int id, int mine) {
      super("RadixSort: id=" + id + " mine=" + mine);
      this.id = id;
      this.mine = mine;
      new Thread(this).start();
      if (debug) System.out.println(getName());
   }

   public void run() {
      int count = 0, other = 0;
      // send my number to all the other workers
      for (int i = 0; i < N; i++) if (i != id) send(channel[i], mine);
      // of the numbers sent by the other workers, count how many are less
      for (int i = 1; i < N; i++) { // only receive N-1 numbers
         other = receiveInt(channel[id]);
         if (other < mine) count++;
      }
      if (debug) System.out.println(getName() + " count=" + count);
      // send my count of less-than-seen back to main()
      send(reply, new Result(mine, count));
   }

   private static void printArray(int[] a) {
      int count = 0;
      for (int i = 0; i < a.length; i++) {
         System.out.print(" " + a[i]); count++;
         if (count % MAX_PER_LINE == 0) System.out.print("\n");
      }
      if (count % MAX_PER_LINE != 0) System.out.print("\n");
   }

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Udn:r:");
      go.optErr = false;
      String usage = "Usage: -d -n N -r RANGE"
         + " nums[i] in [1,RANGE] for i=0,1,...,N";
      int ch = -1;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         } else if ((char)ch == 'd') {
            debug = true;  // go.optErr = true;
         } else if ((char)ch == 'n') {
            N = go.processArg(go.optArgGet(), N);
            if (N < 2) {
               System.err.println("RadixSort, N < 2");
               System.exit(1);
            }
         } else if ((char)ch == 'r') {
            RANGE = go.processArg(go.optArgGet(), RANGE);
            if (RANGE < 10) {
               System.err.println("RadixSort, RANGE < 10");
               System.exit(1);
            }
         } else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("Radix sorting " + N
         + " integers between -" + RANGE + " and " + RANGE
         + " (debug=" + debug + ")\n" +
         " (or get the integers from the command line)");
      nums = new int[N];
      int argNum = go.optIndexGet();
      for (int i = 0; i < N; i++) {
         nums[i] = (int)random(-RANGE, RANGE);
         nums[i] = go.tryArg(argNum++, nums[i]);
      }
      System.out.println("Original numbers:");
      printArray(nums);
      
      reply = new AsyncMessagePassing();  // set up the reply channel      
      channel = new MessagePassing[N];
      for (int i = 0; i < N; i++)    // set up the communication channels
         channel[i] = new AsyncMessagePassing();
      // start the worker threads
      for (int i = 0; i < N; i++) new RadixSort(i, nums[i]);

      int[] tallyCounts = new int[N];
      for (int i = 0; i < N; i++) tallyCounts[i] = 0;
      for (int i = 0; i < N; i++) {         // gather the results
         Result r = (Result) receive(reply);
         nums[r.count] = r.number;
         tallyCounts[r.count]++;
      }
      System.out.println("Sorted   numbers:");
      printArray(nums);
      printArray(tallyCounts); // zeros show where duplicates have occured
      System.out.println("age()=" + age() + ", done");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac rads.java

D:\>java RadixSort
Radix sorting 15 integers between -1000 and 1000 (debug=false)
 (or get the integers from the command line)
Original numbers:
 -696 -308 340 -113 -488 -198 2 341 -911 518 -466 690 2 612 -28
Sorted   numbers:
 -911 -696 -488 -466 -308 -198 -113 -28 2 518 340 341 518 612 690
 1 1 1 1 1 1 1 1 2 0 1 1 1 1 1
age()=220, done
                                            ... end of example run(s)  */
