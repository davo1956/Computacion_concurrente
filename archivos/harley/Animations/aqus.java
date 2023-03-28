import java.awt.*;
import Utilities.*;
import Synchronization.*;
import XtangoAnimation.*;

class Task {
   public int left = -1, right = -1;
   public Task(int left, int right) { this.left = left; this.right = right; }
}

class AnimatedQuickSort extends MyObject implements Runnable {

   private static XtangoAnimator xa = null;
   private static final int MAX_PER_LINE = 15;
   private static int N = 10;
   private static int RANGE = 100;
   private static int NCPU = 4;
   private static boolean debug = false;
   private static final CountingSemaphore doneCount
      = new CountingSemaphore(0); // same as sending empty messages async
   private static final AsyncMessagePassing task
      = new AsyncMessagePassing();
   private static int[] nums = null;

   private int id = -1;

   private AnimatedQuickSort(String name, int id) {
      super(name + id);
      this.id = id;
      new Thread(this).start();
   }

//#### animator #####v   
   private static float scaleX(int x) { return (float)x/(float)N; }

   private static float scaleY(int y) { return (float)y/(float)RANGE; }

   private static int maxx(int[] number, int left, int right) {
      // maximum in number[left] to number[right] inclusive
      int mx = number[left];
      for (int i = left+1; i <= right; i++) {
         if (number[i] > mx) mx = number[i];
      }
      return mx;
   }

   private static int minn(int[] number, int left, int right) {
      // minimum in number[left] to number[right1] inclusive
      int mn = number[left];
      for (int i = left+1; i <= right; i++) {
         if (number[i] < mn) mn = number[i];
      }
      return mn;
   }

   private static final Color[] colors = {Color.red, Color.green,
      Color.blue, Color.yellow, Color.magenta, Color.cyan};
//#### animator #####^

   private static void printArray(int[] a) {
      int count = 0;
      for (int i = 0; i < a.length; i++) {
         System.out.print(" " + a[i]); count++;
         if (count % MAX_PER_LINE == 0) System.out.print("\n");
      }
      if (count % MAX_PER_LINE != 0) System.out.print("\n");
   }

   private static void quickSort(int worker, int left, int right) {

      int pivot = nums[left];
      int l = left, r = right;

      if (right-left <= 0) {
         System.err.println("right-left<=0, error!"); return;
      }

//#### animator #####v
      // enclose what this worker is working on in a black outline rectangle
      int ymin = minn(nums, left, right);
      int ymax = maxx(nums, left, right);
      float xpos, ypos, xsize, ysize;
      xpos = scaleX(left); ypos = scaleY(ymin);
      xsize = scaleX(right-left); ysize = scaleY(ymax-ymin);
      xa.rectangle("rect"+worker, xpos, ypos, xsize, ysize, Color.black,
         xa.OUTLINE);
      xa.delay(1);
      // change items sorted to this worker's color
      for (int i = left; i <= right; i++) {
         xa.color("nums"+i, colors[worker]);   // would be better to do
         xa.fill("nums"+i, xa.SOLID);          // these as a batch i.e.
      }                                        // no frameDelay
      xa.delay(1);
      // make pivot outline color
      xa.fill("nums"+left, xa.OUTLINE);
      // draw a black horizontal line from the pivot across the rectangle
      xpos = scaleX(left); ypos = scaleY(pivot);
      xsize = scaleX(right-left); ysize = 0.0f;
      xa.line("lineH"+worker, xpos, ypos, xsize, ysize, Color.black,
         xa.THIN);
      // draw two vertical lines at left+1 and right
      xpos = scaleX(l+1); ypos = scaleY(pivot);
      xsize = 0.0f; ysize = scaleY(ymax-pivot);
      xa.line("lineVL"+worker, xpos, ypos, xsize, ysize, Color.black,
         xa.THIN);
      xpos = scaleX(r); ypos = scaleY(ymin);
      xsize = 0.0f; ysize = scaleY(pivot-ymin);
      xa.line("lineVR"+worker, xpos, ypos, xsize, ysize, Color.black,
         xa.THIN);
      xa.delay(1);
//#### animator #####^

      boolean done = false;
      while (!done) {
         if (nums[l+1] > pivot) {
            while (r > l+1 && nums[r] > pivot) { r--;

//#### animator #####v
               // move the right vertical line one to the left
               if (!done) {
                  xa.jumpRelative("lineVR"+worker, scaleX(-1), 0.0f);
               }
//#### animator #####^

            }
            if (r > l+1) { l++;
               int temp = nums[r]; nums[r] = nums[l]; nums[l] = temp;
               done = l >= r-1;

//#### animator #####v
               // swap locations and ids of the objects
               xa.moveAsync("nums"+r, scaleX(l), scaleY(nums[l]));
               xa.move("nums"+l, scaleX(r), scaleY(nums[r]));
               xa.swapIds("nums"+r, "nums"+l);
//#### animator #####^

//#### animator #####v
               // move the left vertical line one to the right
               if (!done) {
                  xa.jumpRelative("lineVL"+worker, scaleX(1), 0.0f);
               }
//#### animator #####^

            } else done = true;
         } else { l++; done = l >= r;

//#### animator #####v
            // move the left vertical line one to the right
            if (!done) {
               xa.jumpRelative("lineVL"+worker, scaleX(1), 0.0f);
            }
//#### animator #####^

         }
      }
      int temp = nums[left]; nums[left] = nums[l]; nums[l] = temp;

//#### animator #####v
      // swap locations and ids of the objects
      xa.moveAsync("nums"+left, scaleX(l), scaleY(nums[l]));
      xa.move("nums"+l, scaleX(left), scaleY(nums[left]));
      xa.swapIds("nums"+left, "nums"+l);
//#### animator #####^

      if (right-(l+1) > 0) send(task, new Task(l+1, right));
      else if (right-(l+1) == 0) {  V(doneCount);

//#### animator #####v
         // color the object solid black to indicate it is in final position
         xa.color("nums"+right, Color.black);
         xa.fill("nums"+right, xa.SOLID);
//#### animator #####^

      }

//#### animator #####v
      // delete the line and rectangle objects
      xa.delete("rect"+worker);
      xa.delete("lineH"+worker);
      xa.delete("lineVL"+worker);
      xa.delete("lineVR"+worker);
//#### animator #####^

      V(doneCount);

//#### animator #####v
      // color the object solid black to indicate it is in final position
      xa.color("nums"+l, Color.black);
      xa.fill("nums"+l, xa.SOLID);
//#### animator #####^

      if ((l-1)-left > 0) send(task, new Task(left, l-1));
      else if ((l-1)-left == 0) {  V(doneCount);

//#### animator #####v
         // color the object solid black to indicate it is in final position
         xa.color("nums"+left, Color.black);
         xa.fill("nums"+left, xa.SOLID);
//#### animator #####^

      }
   }

   public void run() {
      Task m = null;
      String ids = String.valueOf(id);
      if (debug)
         System.out.println("*DEBUG* age()=" + age() + ", worker "
            + id + " alive");
      while (true) {
         m = (Task) receive(task);
         if (debug)
            System.out.println("*DEBUG* age()=" + age() + ", worker "
               + id + " received task, left=" + m.left
               + " right=" + m.right);
         xa.fill(ids, xa.SOLID);          //#### animator #####
         quickSort(id, m.left, m.right);
         xa.fill(ids, xa.OUTLINE);        //#### animator #####
      }
   }

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Udn:r:p:");
      go.optErr = false;
      String usage = "Usage: -d -n N -r RANGE -p NCPU"
         + " nums[i] in [1,RANGE] for i=0,1,...,N";
      int ch = -1;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'd') {
            debug = true;
            go.optErr = true;
         }
         else if ((char)ch == 'n')
            N = go.processArg(go.optArgGet(), N);
         else if ((char)ch == 'r')
            RANGE = go.processArg(go.optArgGet(), RANGE);
         else if ((char)ch == 'p')
            NCPU = go.processArg(go.optArgGet(), NCPU);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      if (NCPU > 6) {
         System.err.println("Too many CPUs, must be <= 6, exiting");
         System.exit(1);
      }
      System.out.println("Quick sorting " + N
         + " numbers between 1 and " + RANGE
         + " using " + NCPU + " CPUs (debug=" + debug + ")\n"
         + " (or read POSITIVE numbers from the command line)");
      nums = new int[N];
      int argNum = go.optIndexGet();
      for (int i = 0; i < N; i++) {
         nums[i] = 1 + (int)(Math.random()*RANGE);
         nums[i] = go.tryArg(argNum++, nums[i]);
      }
      System.out.println("Original numbers:");
      printArray(nums);

//#### animator #####v
      xa = new XtangoAnimator();
      xa.begin(); // wait here for user to click "Start"
      // change coordinates so there is room for CPU busy circles
      xa.coords(-0.05f, -0.05f, 1.3f, 1.3f);
      for (int i = 0; i < N; i++) { // display original numbers
         xa.circle("nums"+i, scaleX(i), scaleY(nums[i]), 0.75f*scaleX(1),
            Color.black, xa.OUTLINE);
      }
      xa.delay(1);
      // draw a line separating data from CPU busy circles
      xa.line("CPUline", 0.0f, 1.05f, 1.05f, 0.0f, Color.black, xa.THIN);
      // draw outline circles for each CPU
      for (int i = 0; i < NCPU; i++) {
         String ids = String.valueOf(i);
         xa.circle(ids, 0.1f+0.1f*i, 1.15f, 0.05f, colors[i], xa.OUTLINE);
      }
      xa.text("CPUtext", 0.1f+0.1f*NCPU, 1.15f, false, Color.black,
         "busy CPUs are solid");
      xa.delay(1);
//#### animator #####^

      // create the workers with self-starting threads
      for (int i = 0; i < NCPU; i++) new AnimatedQuickSort("Worker", i);
      System.out.println("age()=" + age() + ", all workers started");
      send(task, new Task(0, N-1));
      // wait for enough "singletons" to be produced
      for (int i = 0; i < N; i++) P(doneCount);
      System.out.println("Sorted   numbers:");
      printArray(nums);
      System.out.println("age()=" + age() + ", done");
      xa.end();                                //#### animator #####
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\Animations>javac aqus.java

D:\Animations>java AnimatedQuickSort -n50 -r10000 -p6
Quick sorting 50 numbers between 1 and 10000 using 6 CPUs (debug=false)
 (or read POSITIVE numbers from the command line)
Original numbers:
 9970 1448 5916 5702 6592 768 3117 8321 3417 7362 673 7521 3963 8754 8161
 5526 6413 7255 2496 5269 2598 6916 714 6876 9685 4056 604 4745 7805 1540
 9891 5850 657 4928 7457 3107 281 9416 8084 8519 1666 110 5619 9631 7624
 4556 8124 9368 9453 5719
XtangoAnimator: Push the Start button
age()=5870, all workers started
Sorted   numbers:
 110 281 604 657 673 714 768 1448 1540 1666 2496 2598 3107 3117 3417
 3963 4056 4556 4745 4928 5269 5526 5619 5702 5719 5850 5916 6413 6592 6876
 6916 7255 7362 7457 7521 7624 7805 8084 8124 8161 8321 8519 8754 9368 9416
 9453 9631 9685 9891 9970
age()=66730, done
XtangoAnimator: Push the Close or Quit button
                                            ... end of example run(s)  */
