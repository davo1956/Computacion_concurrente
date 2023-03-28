import Utilities.*;
import Synchronization.*;

class LaplaceGrid extends MyObject implements Runnable {

   private static int M = 4, N = 5, numIterations = 10;
   private static boolean debug = false;
   private static MessagePassing[][][] grid = null;
   private static MessagePassing done = null;

   private int id = -1, jd = -1;

   private LaplaceGrid(int id, int jd) {
      super("LaplaceGrid: id=" + id + " jd=" + jd);
      this.id = id;  this.jd = jd;
      new Thread(this).start();
      if (debug) System.out.println(getName());
   }

   public void run() {
      double value, got, total, average, correction;
      value = random(10*M*N);
      System.out.println(getName() + " value=" + value);
      for (int iter = 0; iter < numIterations; iter++) {
         if (id < M-2 && jd > 0 && jd < N-1) send(grid[id+1][jd][0], value);
         if (id > 1   && jd > 0 && jd < N-1) send(grid[id-1][jd][1], value);
         if (jd < N-2 && id > 0 && id < M-1) send(grid[id][jd+1][2], value);
         if (jd > 1   && id > 0 && id < M-1) send(grid[id][jd-1][3], value);
         if (debug) System.out.println(getName() + " messages sent");
         total = 0.0;
         if (id > 0 && jd > 0 && id < M-1 && jd < N-1) {
            for (int i = 0; i < 4; i++) {
               got = receiveDouble(grid[id][jd][i]);
               total += got;
            }
            average = total/4.0; correction = average - value;
            value += correction;
// optional delay                    nap(1+(int)random(100*M*N));
            if (debug || iter == 0 || iter == numIterations-1)
               System.out.println("iter=" + iter + ", " + getName() 
                  + " new value=" + value + " (correction was "
                  + correction + ")");
         }
      }
      send(done, 0);
   }

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Udm:n:i:");
      go.optErr = false;
      String usage = "Usage: -d -m M -n N -i numIterations";
      int ch = -1;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         } else if ((char)ch == 'd') {
            debug = true;  // go.optErr = true;
         } else if ((char)ch == 'm') {
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
         + ", numIterations=" + numIterations);
            
      grid = new MessagePassing[M][N][4];  // set up the communication grid
      for (int m = 0; m < M; m++) for (int n = 0; n < N; n++)
         for (int i = 0; i < 4; i++)
            grid[m][n][i] = new AsyncMessagePassing();
      
      done = new PipedMessagePassing();    // start the workers
      for (int m = 0; m < M; m++) for (int n = 0; n < N; n++)
         new LaplaceGrid(m, n);
      for (int m = 0; m < M; m++) for (int n = 0; n < N; n++)
         receiveInt(done);                 // wait for them to finish
      System.out.println("age()=" + age() + " LaplaceGrid done");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac gr4c.java

D:\>java LaplaceGrid -i20
Laplace Grid, M=4, N=5, numIterations=20
LaplaceGrid: id=0 jd=0 value=22.6504
LaplaceGrid: id=0 jd=1 value=33.3639
LaplaceGrid: id=0 jd=3 value=58.6217
LaplaceGrid: id=0 jd=4 value=84.7974
LaplaceGrid: id=1 jd=1 value=166.341
LaplaceGrid: id=1 jd=3 value=138.905
LaplaceGrid: id=1 jd=4 value=92.4468
LaplaceGrid: id=2 jd=0 value=187.156
LaplaceGrid: id=2 jd=1 value=137.341
LaplaceGrid: id=2 jd=2 value=114.465
LaplaceGrid: id=2 jd=3 value=50.7694
LaplaceGrid: id=2 jd=4 value=156.9
LaplaceGrid: id=3 jd=0 value=36.1959
LaplaceGrid: id=3 jd=1 value=143.94
iter=0, LaplaceGrid: id=2 jd=1 new value=152.976 (correction was 15.6342)
LaplaceGrid: id=3 jd=2 value=150.445
LaplaceGrid: id=0 jd=2 value=176.961
LaplaceGrid: id=1 jd=2 value=20.7429
iter=0, LaplaceGrid: id=1 jd=2 new value=149.168 (correction was 128.425)
LaplaceGrid: id=3 jd=3 value=141.21
iter=0, LaplaceGrid: id=1 jd=3 new value=55.6452 (correction was -83.2601)
iter=0, LaplaceGrid: id=2 jd=3 new value=137.87 (correction was 87.1007)
LaplaceGrid: id=1 jd=0 value=46.4443
iter=0, LaplaceGrid: id=2 jd=2 new value=89.8246 (correction was -24.6401)
LaplaceGrid: id=3 jd=4 value=94.5951
iter=0, LaplaceGrid: id=1 jd=1 new value=59.4731 (correction was -106.868)
iter=19, LaplaceGrid: id=1 jd=3 new value=102.869 (correction was 0.00484618)
iter=19, LaplaceGrid: id=2 jd=2 new value=137.371 (correction was 0.00685354)
iter=19, LaplaceGrid: id=2 jd=3 new value=134.585 (correction was -0.00383417)
iter=19, LaplaceGrid: id=2 jd=1 new value=138.63 (correction was -0.00383417)
iter=19, LaplaceGrid: id=1 jd=1 new value=86.065 (correction was 0.00484618)
iter=19, LaplaceGrid: id=1 jd=2 new value=125.812 (correction was -0.00542233)
age()=770 LaplaceGrid done
                                            ... end of example run(s)  */
