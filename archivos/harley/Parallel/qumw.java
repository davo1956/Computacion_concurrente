import Utilities.*;
import Synchronization.*;

class Nqueens extends MyObject implements Runnable {

   private static int N = 8;
   private static int NCPU = 4;
   private static int numSolutions = 0;
   private static PipedMessagePassing getWork = null;
   private static PipedMessagePassing putCount = null;

   private int id = -1;

   private Nqueens(int id) {
      super("Worker" + id);
      this.id = id;
      new Thread(this).start();
   }

   public void run() {
      while (true) {
         int inRow = receiveInt(getWork);
         System.out.println("age()=" + age() + ", " + getName()
            + " counting solutions with column 1 queen in row " + inRow);
         int[] board = new int[N+1];  // need 1 through N (0 unused)
         for (int i = 0; i < board.length; i++) board[i] = 0;
         board[1] = inRow;
         int numFound = place(2, board);
         send(putCount, numFound);
         System.out.println
            ("age()=" + age() + " Solutions with column 1 queen in row "
            + inRow + " = " + numFound);
      }
   }

   private static boolean safe(int row, int column, int[] board) {
      for (int j=1; j<column; j++) {
         if (board[column-j] == row   ||
             board[column-j] == row-j ||
             board[column-j] == row+j) {
            return false;
         }
      }
      return true;
   }

   private static int place(int column, int[] board) {
      int numFound = 0;
      for (int row = 1; row <= N; row++) {
         board[column] = row;
         if (safe(row, column, board)) {
            if (column==N) numFound++;
            else numFound += place(column+1, board);
         }
         board[column] = 0;
      }
      return numFound;
   }

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Un:p:");
      go.optErr = true;
      String usage = "Usage: -n boardSize -p numberCPUs";
      int ch = -1;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'n')
            N = go.processArg(go.optArgGet(), N);
         else if ((char)ch == 'p')
            NCPU = go.processArg(go.optArgGet(), NCPU);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      if (N < 2) {
         System.out.println("board too small, N=" + N);
         System.exit(1);
      }
      if (NCPU < 1) {
         System.out.println("number CPUs too small, NCPU=" + NCPU);
         System.exit(1);
      }
      System.out.println("Nqueens: N=" + N + " NCPU=" + NCPU);
      getWork = new PipedMessagePassing();
      putCount = new PipedMessagePassing();
      // create a worker thread for each CPU
      for (int i = 0; i < NCPU; i++) new Nqueens(i);
      // send out all the "work" (initial configurations)
      // a queen in column 1 and row i
      for (int i = 1; i <= N; i++) send(getWork, i);
      // tally up the returning counts
      for (int row = 1; row <= N; row++) {
         numSolutions += receiveInt(putCount);
      }
      System.out.println("age()=" + age() + ", Nqueens: numSolutions="
         + numSolutions);
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac qumw.java

D:\>java Nqueens -n10
Nqueens: N=10 NCPU=4
age()=0, Worker1 counting solutions with column 1 queen in row 1
age()=0, Worker2 counting solutions with column 1 queen in row 2
age()=0, Worker3 counting solutions with column 1 queen in row 3
age()=0, Worker0 counting solutions with column 1 queen in row 4
age()=2140 Solutions with column 1 queen in row 1 = 64
age()=2250, Worker1 counting solutions with column 1 queen in row 5
age()=2580 Solutions with column 1 queen in row 2 = 48
age()=2690, Worker2 counting solutions with column 1 queen in row 6
age()=2860 Solutions with column 1 queen in row 4 = 93
age()=2970 Solutions with column 1 queen in row 3 = 65
age()=3240, Worker3 counting solutions with column 1 queen in row 8
age()=3020, Worker0 counting solutions with column 1 queen in row 7
age()=4670 Solutions with column 1 queen in row 5 = 92
age()=4730, Worker1 counting solutions with column 1 queen in row 9
age()=5660 Solutions with column 1 queen in row 6 = 92
age()=5710, Worker2 counting solutions with column 1 queen in row 10
age()=5930 Solutions with column 1 queen in row 7 = 93
age()=5990 Solutions with column 1 queen in row 8 = 65
age()=7250 Solutions with column 1 queen in row 9 = 48
age()=7360 Solutions with column 1 queen in row 10 = 64
age()=7580, Nqueens: numSolutions=724
                                            ... end of example run(s)  */
