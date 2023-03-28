import Utilities.*;
import Synchronization.*;

class Result { public int position, value;
   public Result(int p, int v) { position = p; value = v; }
}

class Worker extends MyObject implements Runnable {
   // A worker receives m integers on myPipe from its predecessor.
   // It keeps smallest and sends others on to the next worker.
   // After seeing all m integers, worker sends smallest to sort,
   // together with the position (m) smallest is to be placed.

   public static AsyncMessagePassing create
         (int m, AsyncMessagePassing result) {
      AsyncMessagePassing myPipe = new AsyncMessagePassing();
      Worker worker = new Worker(m, result, myPipe);
      (new Thread(worker)).start();
      return myPipe;
   }

   private int m = -1;
   private AsyncMessagePassing result = null;
   private AsyncMessagePassing myPipe = null;

   // can only be called by create() method
   private Worker(int m, AsyncMessagePassing result,
         AsyncMessagePassing myPipe) {
      this.m = m;
      this.result = result;
      this.myPipe = myPipe;
   }

   public void run() {
      int candidate, smallest;   // the smallest seen so far
      smallest = receiveInt(myPipe);
      if (m > 1) {
         AsyncMessagePassing nextWorker;  // pipe to next worker
         // create next instance of worker
         nextWorker = Worker.create(m-1, result);
         for (int i = m-1; i > 0; i--) {
            candidate = receiveInt(myPipe);
            // save new value if it is smallest so far;
            // send other values on
            if (candidate < smallest) {
               int temp = candidate;
               candidate = smallest; smallest = temp;
            }
            send(nextWorker, candidate);
         }
      }
      send(result, new Result(m, smallest)); // return smallest to sort
   }
}

class PipelineSort extends MyObject {

   private static final int MAX_PER_LINE = 10;
   private static int N = 10;
   private static int RANGE = 100;
   private static int[] nums = null;

   private static void printArray(int[] a) {
      int count = 0;
      for (int i = 0; i < a.length; i++) {
         System.out.print(" " + a[i]); count++;
         if (count % MAX_PER_LINE == 0) System.out.print("\n");
      }
      if (count % MAX_PER_LINE != 0) System.out.print("\n");
   }

   private static void sort(int[] a) {
      int position, value;
      AsyncMessagePassing result = new AsyncMessagePassing();
      if (a.length == 0) return;
      AsyncMessagePassing firstWorker = null;
      // Create first worker; get back a reference for its pipe operation,
      //    then use the pipe to send all values in a to the worker.
      firstWorker = Worker.create(a.length, result);
      for (int i = 0; i < a.length; i++) send(firstWorker, a[i]);
      // Gather the results and place them in the right place in a
      for (int i = 0; i < a.length; i++) {
            Result r = (Result) receive(result);
            a[a.length-r.position] = r.value;
      }
   }

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Un:r:");
      go.optErr = false;
      String usage = "Usage: -n N -r RANGE"
         + " nums[i] in [1,RANGE] for i=0,1,...,N";
      int ch = -1;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'n')
            N = go.processArg(go.optArgGet(), N);
         else if ((char)ch == 'r')
            RANGE = go.processArg(go.optArgGet(), RANGE);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("Pipeline sorting " + N
         + " numbers between " + (-RANGE) + " and " + RANGE);
      nums = new int[N];
      int argNum = go.optIndexGet();
      for (int i = 0; i < N; i++) {
         nums[i] = (int)(random(-RANGE, RANGE));
         nums[i] = go.tryArg(argNum++, nums[i]);
      }
      System.out.println("Original numbers:");
      printArray(nums);
      sort(nums);
      System.out.println("Sorted   numbers:");
      printArray(nums);
      System.out.println("age()=" + age() + ", done");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac psrt.java

D:\>java PipelineSort -n5 5 4 3 2 1
Pipeline sorting 5 numbers between -100 and 100
Original numbers:
 5 4 3 2 1
Sorted   numbers:
 1 2 3 4 5
age()=160, done

D:\>java PipelineSort -n100 -r10000
Pipeline sorting 100 numbers between -10000 and 10000
Original numbers:
 -5375 150 -8467 1965 -5604 -3445 3778 1807 -4732 -3439
 -1284 2625 -2744 6966 2073 4353 -9744 496 8694 -3876
 -138 -5477 5601 -9923 6316 9847 -3852 1206 -6135 -1968
 -4053 -1319 7311 8608 -1330 -2772 -7234 -4052 7735 -4013
 354 -360 -1409 6548 8120 -8024 -4530 1999 8359 -8590
 -3595 4381 334 -8118 759 9262 -3394 -5573 4638 3026
 -5861 9723 -8709 966 5898 8066 -4008 2462 -6172 6693
 7839 3254 9894 3490 4572 4232 -1675 -9205 686 -8814
 -815 -6635 4061 -9409 1165 -9476 1734 -7202 -303 4075
 -8380 -4147 5473 800 3672 735 -1991 -8840 -8608 1912
Sorted   numbers:
 -9923 -9744 -9476 -9409 -9205 -8840 -8814 -8709 -8608 -8590
 -8467 -8380 -8118 -8024 -7234 -7202 -6635 -6172 -6135 -5861
 -5604 -5573 -5477 -5375 -4732 -4530 -4147 -4053 -4052 -4013
 -4008 -3876 -3852 -3595 -3445 -3439 -3394 -2772 -2744 -1991
 -1968 -1675 -1409 -1330 -1319 -1284 -815 -360 -303 -138
 150 334 354 496 686 735 759 800 966 1165
 1206 1734 1807 1912 1965 1999 2073 2462 2625 3026
 3254 3490 3672 3778 4061 4075 4232 4353 4381 4572
 4638 5473 5601 5898 6316 6548 6693 6966 7311 7735
 7839 8066 8120 8359 8608 8694 9262 9723 9847 9894
age()=1650, done
                                            ... end of example run(s)  */
