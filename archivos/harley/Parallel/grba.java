import Utilities.*;
import Synchronization.*;

class LaplaceGrid extends MyObject implements Runnable {

   private static int M = 4, N = 5, numIterations = 10;
   private static boolean debug = false;
   private static boolean grind = false;
   private static double[][] grid = null;
   private static Barrier b = null;

   private int id = -1, jd = -1;
   private Thread thread = null;

   private LaplaceGrid(int id, int jd) {
      super("LaplaceGrid: id=" + id + " jd=" + jd);
      this.id = id;  this.jd = jd;
      thread = new Thread(this);  thread.start();
      if (debug) System.out.println(getName());
   }

   public void join() {
      try { thread.join(); } catch (InterruptedException e) {}
   }

   public void run() {
      double value, total, average, correction;
      grid[id][jd] = value = random(10*M*N);
      System.out.println(getName() + " initial value=" + value);

      b.gate(id, jd); // Why do we call the barrier here?

      for (int iter = 0; iter < numIterations; iter++) {
         if (id > 0 && jd > 0 && id < M-1 && jd < N-1) {
            total  = grid[id+1][jd];
            total += grid[id-1][jd];
            total += grid[id][jd+1];
            total += grid[id][jd-1];
            average = total/4.0; correction = average - value;
            value += correction;

            // optional delay to simulate grinding commputation
            if (grind) nap(1+(int)random(100*M*N));

            if (debug || iter == 0 || iter == numIterations-1)
               System.out.println("iter=" + iter + ", " + getName() 
                  + " new value=" + value + " (correction was "
                  + correction + ")");
         }
         if (debug)
            System.out.println(getName() + " at barrier " + (id+jd*M));
         b.gate(id, jd);
         if (id > 0 && jd > 0 && id < M-1 && jd < N-1)
            grid[id][jd] = value;

         b.gate(id, jd); // Why do we call the barrier again?
      }
   }

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Udgm:n:i:");
      go.optErr = true;
      String usage = "Usage: -d -g -m M -n N -i numIterations";
      int ch = -1;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         } else if ((char)ch == 'd') debug = true;
         else if ((char)ch == 'g') grind = true;
         else if ((char)ch == 'm') {
            M = go.processArg(go.optArgGet(), M);
            if (M < 3) {
               System.err.println("LaplaceGrid, M < 3");
               System.exit(1);
            }
         } else if ((char)ch == 'n') {
            N = go.processArg(go.optArgGet(), N);
            if (N < 3) {
               System.err.println("LaplaceGrid, N < 3");
               System.exit(1);
            }
         } else if ((char)ch == 'i') {
            numIterations = go.processArg(go.optArgGet(), numIterations);
            if (numIterations < 3) {
               System.err.println("LaplaceGrid, numIterations < 3");
               System.exit(1);
            }
         } else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("Laplace Grid, M=" + M + ", N=" + N
         + ", numIterations=" + numIterations + ", grind=" + grind);
      
      b = new Barrier(M, N);  // set up the barrier      
      grid = new double[M][N];  // set up the grid
      for (int m = 0; m < M; m++) for (int n = 0; n < N; n++)
         grid[m][n] = 0;      
      LaplaceGrid[][] lg = new LaplaceGrid[M][N];  // start the workers
      for (int m = 0; m < M; m++) for (int n = 0; n < N; n++)
         lg[m][n] = new LaplaceGrid(m, n);      
      for (int m = 0; m < M; m++) for (int n = 0; n < N; n++)
         lg[m][n].join();           // wait for them to finish
      System.out.println("age()=" + age() + " LaplaceGrid done");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac grba.java

D:\>java LaplaceGrid -g
Laplace Grid, M=4, N=5, numIterations=10, grind=true
LaplaceGrid: id=0 jd=0 initial value=7.01462
LaplaceGrid: id=0 jd=1 initial value=118.19
LaplaceGrid: id=2 jd=3 initial value=7.03291
LaplaceGrid: id=0 jd=2 initial value=0.152569
LaplaceGrid: id=0 jd=3 initial value=104.728
LaplaceGrid: id=0 jd=4 initial value=64.8324
LaplaceGrid: id=1 jd=0 initial value=23.2517
LaplaceGrid: id=1 jd=1 initial value=180.331
LaplaceGrid: id=1 jd=2 initial value=85.1332
LaplaceGrid: id=1 jd=3 initial value=85.0098
LaplaceGrid: id=1 jd=4 initial value=176.927
LaplaceGrid: id=2 jd=0 initial value=150.13
LaplaceGrid: id=2 jd=1 initial value=132.37
LaplaceGrid: id=2 jd=2 initial value=93.5912
LaplaceGrid: id=2 jd=4 initial value=181.274
LaplaceGrid: id=3 jd=0 initial value=8.17505
LaplaceGrid: id=3 jd=1 initial value=196.527
LaplaceGrid: id=3 jd=2 initial value=146.04
LaplaceGrid: id=3 jd=3 initial value=143.607
LaplaceGrid: id=3 jd=4 initial value=138.131
iter=0, LaplaceGrid: id=1 jd=1 new value=89.7361 (correction was -90.5946)
iter=0, LaplaceGrid: id=2 jd=3 new value=125.87 (correction was 118.838)
iter=0, LaplaceGrid: id=2 jd=1 new value=155.145 (correction was 22.7747)
iter=0, LaplaceGrid: id=1 jd=2 new value=89.7711 (correction was 4.63785)
iter=0, LaplaceGrid: id=2 jd=2 new value=92.644 (correction was -0.947165)
iter=0, LaplaceGrid: id=1 jd=3 new value=93.4554 (correction was 8.44564)
iter=9, LaplaceGrid: id=2 jd=1 new value=142.381 (correction was -0.222012)
iter=9, LaplaceGrid: id=1 jd=3 new value=129.005 (correction was 0.393865)
iter=9, LaplaceGrid: id=1 jd=1 new value=93.0666 (correction was 0.393498)
iter=9, LaplaceGrid: id=2 jd=3 new value=145.922 (correction was -0.221635)
iter=9, LaplaceGrid: id=2 jd=2 new value=130.752 (correction was 0.55675)
iter=9, LaplaceGrid: id=1 jd=2 new value=87.9081 (correction was -0.313706)
age()=18940 LaplaceGrid done
                                            ... end of example run(s)  */
