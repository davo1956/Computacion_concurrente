import Utilities.*;
import Synchronization.*;

class Multiply extends MyObject implements Runnable {

   private int n = 0;
   private int id = -1, jd = -1;
   private double a = 0.0;
   private MessagePassing north = null, east = null,
      south = null, west = null;

   public Multiply(int n, int id, int jd, double a, MessagePassing north,
         MessagePassing east, MessagePassing south, MessagePassing west) {
      super("Multiply n=" + n + " id=" + id + " jd=" + jd + " a=" + a);
      this.n = n;
      this.id = id;  this.jd = jd;
      this.a = a;
      this.north = north;  this.east = east;
      this.south = south;  this.west = west;
      new Thread(this).start();
   }

   public void run() {
      double sum, x;
      for (int i = 0; i < n; i++) {
         x = receiveDouble(north);
         send(south, x);
         sum = receiveDouble(east);
         sum += a*x;
         send(west, sum);
      }
   }
}

class SystolicMatrixMultiply extends MyObject {

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Ul:m:n:");
      String usage = "Usage: -l L -m M -n N"
         + " a[l,m] l=0..L-1 m=0..M-1, b[m,n] m=0..M-1 n=0..N-1";
      go.optErr = false;
      int ch = -1;
      int L = 0, M = 0, N = 0;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         } else if ((char)ch == 'l') {
            L = go.processArg(go.optArgGet(), L);
            if (L < 1) {
               System.err.println("SystolicMatrixMultiply, L < 1");
               System.exit(1);
            }
         } else if ((char)ch == 'm') {
            M = go.processArg(go.optArgGet(), M);
            if (M < 1) {
               System.err.println("SystolicMatrixMultiply, M < 1");
               System.exit(1);
            }
         } else if ((char)ch == 'n') {
            N = go.processArg(go.optArgGet(), N);
            if (N < 1) {
               System.err.println("SystolicMatrixMultiply, N < 1");
               System.exit(1);
            }
         } else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("MatrixMultiply: L=" + L + " M=" + M + " N=" + N);
      double[][] a = new double[L][M];
      double[][] b = new double[M][N];
      double[][] c = new double[L][N];
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

      // create the communication channels
      MessagePassing[][] channelN  = new MessagePassing[L+1][M+1];
      MessagePassing[][] channelW  = new MessagePassing[L+1][M+1];
      for (int l = 0; l <= L; l++) for (int m = 0; m <= M; m++) {
         channelN[l][m] = new AsyncMessagePassing();
         channelW[l][m] = new AsyncMessagePassing();
      }

      // start a thread for each a[l,m]
      for (int l = 0; l < L; l++) for (int m = 0; m < M; m++) { 
         new Multiply(N, l, m, a[l][m],
         channelN[l][m], channelW[l][m+1], channelN[l+1][m], channelW[l][m]);
      }

      // send columns of b[][] into the source channels along the top
      for (int n = 0; n < N; n++) for (int m = 0; m < M; m++)
         send(channelN[0][m], b[m][n]);

      // send zeros into the left side of the systolic array
      for (int n = 0; n < N; n++) for (int l = 0; l < L; l++)
         send(channelW[l][M], 0.0);

      // throw away the stuff coming out the bottom (was put in the top)
      for (int n = 0; n < N; n++) for (int m = 0; m < M; m++)
         receiveDouble(channelN[L][m]);

      // gather the results into c[l,n]
      for (int n = 0; n < N; n++) for (int l = 0; l < L; l++)
         c[l][n] = receiveDouble(channelW[l][0]);

      // print out the result of the matrix multiply
      System.out.println("c =");
      for (int l = 0; l < L; l++) {
         for (int n = 0; n < N; n++) System.out.print(" " + c[l][n]);
         System.out.println();
      }
      System.out.println("age()=" + age() + " SystolicMatrixMultiply done");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac mats.java

D:\>java SystolicMatrixMultiply \
   -l2 -m3 -n4 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18
MatrixMultiply: L=2 M=3 N=4
a =
 1 2 3
 4 5 6
b =
 7 8 9 10
 11 12 13 14
 15 16 17 18
c =
 74 80 86 92
 173 188 203 218
age()=210 SystolicMatrixMultiply done
                                            ... end of example run(s)  */
