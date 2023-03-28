import Utilities.*;
import Synchronization.*;

class QuickSort extends MyObject implements Runnable {

   private static final int MAX_PER_LINE = 15;
   private static int N = 15;
   private static int RANGE = 1000;
   private static boolean debug = false;
   private static int[] nums = null;

   private int left = -1, right = -1;

   private QuickSort(int left, int right) {
      super("QuickSort: " + left + " to " + right);
      this.left = left;  this.right = right;
   }

   private static Thread QuickSortThread(int left, int right) {
      QuickSort qs = new QuickSort(left, right);
      Thread qst = new Thread(qs);
      qst.start();
      return qst;
   }

   public void run() {
      int pivot = nums[left];
      int l = left, r = right;
      Thread qsl = null, qsr = null;  // for the "recursive" threads
      if (debug)
         System.out.println("*DEBUG* age()=" + age() + ", " + getName());
      if (right-left <= 0) {  // not supposed to happen
         System.err.println("right-left<=0, error!");
         return;
      }

      boolean done = false;
      while (!done) {
         if (nums[l+1] > pivot) {  // needs to be moved to other end of nums
            while (r > l+1 && nums[r] > pivot) { r--;  // find one to swap
            }
            if (r > l+1) { l++;
               int temp = nums[r];       // swap
               nums[r] = nums[l]; nums[l] = temp;
               done = l >= r-1;
            } else done = true;          // if can't find one to swap, then
         } else { l++; done = l >= r;    // need not be moved to other end
         }
      }  // when this loop finishes, nums[left] is the pivot,
         // nums[left:l] <= pivot and nums[l+1,right] > pivot
         //
                              //   [pivot, <= | > ]
                              //    ^        ^ ^ ^
                              //    |        | | |
                              // left        l r right
                              //    |        | | |
                              //    v        v v v
                              //   [<=, pivot | > ]
      int temp = nums[left];                          // swap
      nums[left] = nums[l]; nums[l] = temp;
         // nums[left,l-1] <= pivot,
         // nums[l] == pivot, and
         // nums[l+1,right] > pivot

      // start the "recursive" threads, if any
      if (right-(l+1) > 0) qsr = QuickSortThread(l+1, right);
      // else nums[l+1:right] is singleton and already sorted

      // nums[l] = pivot is singleton so sorted

      if ((l-1)-left > 0) qsl = QuickSortThread(left, l-1);
      // else nums[left:l-1] is singleton and already sorted

      try {   // and wait for them to finish
         if (qsl != null) qsl.join();
         if (qsr != null) qsr.join();
      } catch (InterruptedException e) {
         System.err.println("QuickSort Exception " + e);
      }
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
               System.err.println("QuickSort, N < 2");
               System.exit(1);
            }
         } else if ((char)ch == 'r') {
            RANGE = go.processArg(go.optArgGet(), RANGE);
            if (RANGE < 10) {
               System.err.println("QuickSort, RANGE < 10");
               System.exit(1);
            }
         } else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("Quick sorting " + N
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

      Thread qst = QuickSortThread(0, N-1); // start up the first thread
      try {                                 // and wait for it to finish
         qst.join();
      } catch (InterruptedException e) {
         System.err.println("QuickSort Exception " + e);
      }
      System.out.println("Sorted   numbers:");
      printArray(nums);
      System.out.println("age()=" + age() + ", done");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac qsrt.java

D:\>java QuickSort -d
Quick sorting 15 integers between -1000 and 1000 (debug=true)
 (or get the integers from the command line)
Original numbers:
 375 -376 383 6 -34 -164 -245 -465 704 -52 -420 -437 815 -607 503
*DEBUG* age()=0, QuickSort: 0 to 14
*DEBUG* age()=50, QuickSort: 11 to 14
*DEBUG* age()=50, QuickSort: 0 to 9
*DEBUG* age()=110, QuickSort: 11 to 12
*DEBUG* age()=110, QuickSort: 4 to 9
*DEBUG* age()=110, QuickSort: 0 to 2
*DEBUG* age()=110, QuickSort: 4 to 7
*DEBUG* age()=160, QuickSort: 5 to 7
Sorted   numbers:
 -607 -465 -437 -420 -376 -245 -164 -52 -34 6 375 383 503 704 815
age()=220, done
                                            ... end of example run(s)  */
