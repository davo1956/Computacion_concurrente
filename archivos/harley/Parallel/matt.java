import Utilities.*;
import Synchronization.*;

class MatrixMultiply extends MyObject implements Runnable {

   private static int L = 0, M = 0, N = 0;
   private static double[][] a = null, b = null, c = null;
   private static CountingSemaphore done = new CountingSemaphore(0);

   private int l = 0, n = 0;

   private MatrixMultiply(int l, int n) {
      super("MatrixMultiply: c[" + l + "," + n + "]");
      this.l = l;  this.n = n;
      new Thread(this).start();
   }

   public void run() {
      double innerProduct = 0.0;
      for (int m = 0; m < M; m++) innerProduct += a[l][m]*b[m][n];
      c[l][n] = innerProduct;
      V(done);
   }

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Ul:m:n:");
      go.optErr = false;
      String usage = "Usage: -l L -m M -n N"
         + " a[l,m] l=0..L-1 m=0..M-1, b[m,n] m=0..M-1 n=0..N-1";
      int ch = -1;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         } else if ((char)ch == 'l') {
            L = go.processArg(go.optArgGet(), L);
            if (L < 1) {
               System.err.println("MatrixMultiply, L < 1");
               System.exit(1);
            }
         } else if ((char)ch == 'm') {
            M = go.processArg(go.optArgGet(), M);
            if (M < 1) {
               System.err.println("MatrixMultiply, M < 1");
               System.exit(1);
            }
         } else if ((char)ch == 'n') {
            N = go.processArg(go.optArgGet(), N);
            if (N < 1) {
               System.err.println("MatrixMultiply, N < 1");
               System.exit(1);
            }
         } else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("MatrixMultiply: L=" + L + " M=" + M + " N=" + N);
      a = new double[L][M];  b = new double[M][N]; c = new double[L][N];
      // get the matrices to multiply from the command line
      int argNum = go.optIndexGet();
      for (int l = 0; l < L; l++) for (int m = 0; m < M; m++)
         a[l][m] = go.tryArg(argNum++, 0.0 /*default*/);
      for (int m = 0; m < M; m++) for (int n = 0; n < N; n++)
         b[m][n] = go.tryArg(argNum++, 0.0 /*default*/);

      // print out the matrices to be multiplied
      System.out.println("a =");
      for (int l = 0; l < L; l++) {
         for (int m = 0; m < M; m++) System.out.print(" " + a[l][m]);
         System.out.println();
      }
      System.out.println("b =");
      for (int m = 0; m < M; m++) {
         for (int n = 0; n < N; n++) System.out.print(" " + b[m][n]);
         System.out.println();
      }

      // start a thread to compute each c[l,n]
      for (int l = 0; l < L; l++) for (int n = 0; n < N; n++)
         new MatrixMultiply(l, n);

      // wait for them to finish
      for (int l = 0; l < L; l++) for (int n = 0; n < N; n++) P(done);

      // print out the result of the matrix multiply
      System.out.println("c =");
      for (int l = 0; l < L; l++) {
         for (int n = 0; n < N; n++) System.out.print(" " + c[l][n]);
         System.out.println();
      }
   }
}

/* ............... Example compile and run(s)

D:\>javac matt.java

D:\>java MatrixMultiply -l4 -m3 -n2 20 19 18 17 16 15 14 13 12 11 10 9 8 7 6 5 4 3
MatrixMultiply: L=4 M=3 N=2
a =
 20 19 18
 17 16 15
 14 13 12
 11 10 9
b =
 8 7
 6 5
 4 3
c =
 346 289
 292 244
 238 199
 184 154
                                            ... end of example run(s)  */
