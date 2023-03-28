import java.awt.*;

public class AnimatedBubbleSort {

   private static final int MAX_PER_LINE = 15;
   private static XtangoApplet xa = null;
   private static int N = 10;
   private static int RANGE = 100;

   private static float scaleX(int x) { return (float)x/(float)N; }

   private static float scaleY(int y) { return (float)y/(float)RANGE; }

   private static void printArray(int[] a) {
      int count = 0;
      for (int i = 0; i < a.length; i++) {
         System.out.print(" " + a[i]); count++;
         if (count % MAX_PER_LINE == 0) System.out.print("\n");
      }
      if (count % MAX_PER_LINE != 0) System.out.print("\n");
   }

   private static void bubbleSort(int[] a) { // into non-decreasing order
      for (int i = a.length - 1; i >= 0; i--) {
         for (int j = 0; j < i; j++) {
            xa.color("C"+j, Color.blue);     // make candidate to
            xa.fill("C"+j, xa.SOLID);        // bubble solid blue
            xa.delay(1);
            // momentarily connect the two icons compared with a line
            xa.pointLine("temp", scaleX(j), scaleY(a[j]), scaleX(j+1),
               scaleY(a[j+1]), Color.green, xa.THICK);
            xa.delay(1);
            if (a[j] > a[j+1]) {             // swap the icon locations
               xa.color("temp", Color.red);
               xa.delay(1);
               xa.moveAsync("C"+j, scaleX(j+1), scaleY(a[j]));
               xa.move("C"+(j+1), scaleX(j), scaleY(a[j+1]));
               xa.swapIds("C"+j, "C"+(j+1)); // swap ids of the icons
               int temp = a[j+1]; a[j+1] = a[j]; a[j] = temp;
            } else {
               xa.fill("C"+j, xa.OUTLINE);   // make the smaller icon ...
               xa.color("C"+j, Color.red);      // ... outline red ...
               xa.delay(1);
               xa.color("C"+(j+1), Color.blue); // ... and the larger ...
               xa.fill("C"+(j+1), xa.SOLID);    // ... solid blue
               xa.delay(1);
            }
            xa.delete("temp");
         }
         // color the icon black to indicate it is in final position
         xa.color("C"+i, Color.black);
         xa.delay(1);
      }
      xa.fill("C0", xa.SOLID);
      xa.delay(1);
   }

   public AnimatedBubbleSort(XtangoApplet a) { xa = a; }

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Un:r:");
      go.optErr = false;
      String usage = "Usage: -n N -r RANGE"
         + " nums[i] in [1,RANGE] for i=0,1,...,N";
      int ch = -1;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') { System.out.println(usage); }
         else if ((char)ch == 'n')
            N = go.processArg(go.optArgGet(), N);
         else if ((char)ch == 'r')
            RANGE = go.processArg(go.optArgGet(), RANGE);
         else { System.err.println(usage); }
      }
      System.out.println("Bubble sorting " + N
         + " numbers between 1 and " + RANGE + "\n"
         + " (or read POSITIVE numbers from the command line)");
      int[] nums = new int[N];
      xa.begin(); // wait here for user to click "Start"
      // change coordinates so there is room around the edges
      xa.coords(-0.1f, 0.0f, (float)1, 1.1f);
      int argNum = go.optIndexGet();
      for (int i = 0; i < N; i++) {
         nums[i] = 1 + (int)(Math.random()*RANGE);
         nums[i] = go.tryArg(argNum++, nums[i]);
      }
      System.out.println("Original numbers:");
      printArray(nums);
      for (int i = 0; i < N; i++) { // display original numbers
         xa.circle("C"+i, scaleX(i), scaleY(nums[i]), 0.75f*scaleX(1),
            Color.red, xa.OUTLINE);
      }
      xa.delay(1);
      bubbleSort(nums);
      System.out.println("Sorted   numbers:");
      printArray(nums);
      xa.delay(1);
      xa.end();
   }
}
