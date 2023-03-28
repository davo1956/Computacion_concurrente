import Utilities.*;
import Synchronization.*;

class Result { public int position; public double value;
   public Result(int p, double v) { position = p; value = v; }
}

class Worker extends MyObject implements Runnable {

   private int id = -1;
   private int numWorkers = -1;
   private boolean debug = false;
   private MessagePassing sync = null;
   private MessagePassing result = null;
   private MessagePassing leftIn = null, rightIn = null;
   private MessagePassing leftOut = null, rightOut = null;

   public Worker(int id, int numWorkers, boolean debug, MessagePassing sync,
         MessagePassing leftIn, MessagePassing rightIn,
         MessagePassing leftOut, MessagePassing rightOut) {
      this.id = id;
      this.numWorkers = numWorkers;
      this.debug = debug;
      this.sync = sync;
      this.leftIn = leftIn;
      this.rightIn = rightIn;
      this.leftOut = leftOut;
      this.rightOut = rightOut;
      (new Thread(this)).start();
   }

   public void run() {
      double a, b, c, d;
      a = receiveDouble(leftIn);  b = receiveDouble(rightIn);
      // wait here until all workers have their (a,b)
      result = (MessagePassing) receive(sync);
      if (debug)
      System.out.println("worker " + id + " received " + a + " and " + b);

/* You put code for numWorkers iterations of the compare-exchange here. */

/************************************************************************/

      // Sorting is now complete.
      if (debug)
      System.out.println("worker " + id + " sending " + a + " and " + b);
      send(result, new Result(2*id, a));
      send(result, new Result(2*id+1, b));
   }
}

class CompareExchangeSort extends MyObject {

   private static final int MAX_PER_LINE = 10;

   private static void printArray(double[] a) {
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
      boolean debug = false;
      int N = 10;
      int RANGE = 100;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'd') debug = true;
         else if ((char)ch == 'n')
            N = go.processArg(go.optArgGet(), N);
         else if ((char)ch == 'r')
            RANGE = go.processArg(go.optArgGet(), RANGE);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      if (N <= 0 || N % 2 != 0) {
         System.out.println("number " + N + " to sort must be >0 and even");
         System.exit(1);
      } else System.out.println("CompareExchange sorting " + N
         + " numbers between " + (-RANGE) + " and " + RANGE
         + " (debug=" + debug + ")");
      double[] nums = new double[N];
      int argNum = go.optIndexGet();
      for (int i = 0; i < N; i++) {
         nums[i] = random(-RANGE, RANGE);
         nums[i] = go.tryArg(argNum++, nums[i]);
      }
      int numWorkers = N/2;
      // worker i receives left with left[i] and receives right with right[i]
      AsyncMessagePassing[] left = new AsyncMessagePassing[numWorkers];
      AsyncMessagePassing[] right = new AsyncMessagePassing[numWorkers];
      AsyncMessagePassing sync = new AsyncMessagePassing();
      AsyncMessagePassing result = new AsyncMessagePassing();
      for (int i = 0; i < numWorkers; i++) {
         left[i] = new AsyncMessagePassing();
         right[i] = new AsyncMessagePassing();
      }
      for (int i = 0; i < numWorkers; i++) {
         if (i == 0) { // leftmost worker
//                                leftIn, rightIn, leftOut, rightOut
            new Worker(i, numWorkers, debug, sync,
                                 left[i], right[i], left[i], left[i+1]);
         } else if (i == numWorkers-1) { // rightmost worker
            new Worker(i, numWorkers, debug, sync,
                                 left[i], right[i], right[i-1], right[i]);
         } else { // general case
            new Worker(i, numWorkers, debug, sync,
                                 left[i], right[i], right[i-1], left[i+1]);
         }
      }
      // It is necessary for all the workers to receive their initial
      // two numbers from their leftIn and rightIn channels before they
      // start sending numbers to each other.
      for (int i = 0; i < numWorkers; i++) {
         send(left[i], nums[2*i]);  send(right[i], nums[2*i+1]);
      }
      // Release workers so they can compare and exchange after getting
      // their initial two numbers.
      for (int i = 0; i < numWorkers; i++) send(sync, result);
      System.out.println("Original numbers:");
      printArray(nums);
      for (int i = 0; i < N; i++) {
         Result r = (Result) receive(result);
         nums[r.position] = r.value;
      }
      System.out.println("Sorted   numbers:");
      printArray(nums);
      System.out.println("age()=" + age() + ", done");
      System.exit(0);
   }
}
