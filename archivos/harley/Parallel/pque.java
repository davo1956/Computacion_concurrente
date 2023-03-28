import Utilities.*;
import Synchronization.*;

class nQueens extends MyObject implements Runnable {

   private static int N = 8;
   private static int numSolutions = 0;
   private static PipedMessagePassing channel = null;

   private int inRow = -1;

   private nQueens(int inRow) {
      this.inRow = inRow;
      new Thread(this).start();
   }

   public void run() {
      System.out.println("age()=" + age()
         + " Counting solutions with column 1 queen in row " + inRow);
      int[] board = new int[N+1];
      for (int i = 0; i < board.length; i++) board[i] = 0;
      board[1] = inRow;
      int numFound = place(2, board);
      send(channel, numFound);
      System.out.println
         ("age()=" + age() + " Solutions with column 1 queen in row "
         + inRow + " = " + numFound);
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
      GetOpt go = new GetOpt(args, "Un:");
      go.optErr = true;
      String usage = "Usage: -n boardSize";
      int ch = -1;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'n')
            N = go.processArg(go.optArgGet(), N);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      if (N < 1) {
         System.out.println("board too small, N=" + N);
         System.exit(1);
      }
      System.out.println("nQueens: N=" + N);
      channel = new PipedMessagePassing();
      for (int row = 1; row <= N; row++) new nQueens(row);
      for (int row = 1; row <= N; row++) {
         numSolutions += receiveInt(channel);
      }
      System.out.println("nQueens: numSolutions=" + numSolutions);
   }
}

/* ............... Example compile and run(s)

D:\>javac pque.java

D:\>java nQueens
nQueens: N=8
age()=110 Counting solutions with column 1 queen in row 1
age()=160 Counting solutions with column 1 queen in row 2
age()=160 Solutions with column 1 queen in row 1 = 4
age()=160 Solutions with column 1 queen in row 2 = 8
age()=220 Counting solutions with column 1 queen in row 3
age()=270 Counting solutions with column 1 queen in row 5
age()=220 Counting solutions with column 1 queen in row 4
age()=330 Counting solutions with column 1 queen in row 6
age()=330 Solutions with column 1 queen in row 5 = 18
age()=330 Solutions with column 1 queen in row 4 = 18
age()=380 Solutions with column 1 queen in row 6 = 16
age()=380 Solutions with column 1 queen in row 3 = 16
age()=380 Counting solutions with column 1 queen in row 8
age()=440 Solutions with column 1 queen in row 8 = 4
age()=440 Counting solutions with column 1 queen in row 7
age()=490 Solutions with column 1 queen in row 7 = 8
nQueens: numSolutions=92

D:\>java nQueens -n 10
nQueens: N=10
age()=110 Counting solutions with column 1 queen in row 1
age()=110 Counting solutions with column 1 queen in row 2
age()=160 Counting solutions with column 1 queen in row 3
age()=160 Counting solutions with column 1 queen in row 4
age()=220 Counting solutions with column 1 queen in row 5
age()=220 Counting solutions with column 1 queen in row 6
age()=270 Counting solutions with column 1 queen in row 7
age()=270 Counting solutions with column 1 queen in row 8
age()=330 Counting solutions with column 1 queen in row 9
age()=330 Counting solutions with column 1 queen in row 10
age()=6040 Solutions with column 1 queen in row 1 = 64
age()=6100 Solutions with column 1 queen in row 4 = 93
age()=6320 Solutions with column 1 queen in row 10 = 64
age()=6540 Solutions with column 1 queen in row 5 = 92
age()=6370 Solutions with column 1 queen in row 6 = 92
age()=6700 Solutions with column 1 queen in row 8 = 65
age()=6700 Solutions with column 1 queen in row 9 = 48
age()=6810 Solutions with column 1 queen in row 7 = 93
age()=6920 Solutions with column 1 queen in row 3 = 65
age()=6920 Solutions with column 1 queen in row 2 = 48
nQueens: numSolutions=724
                                            ... end of example run(s)  */
