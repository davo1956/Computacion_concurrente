// Generate all possible solutions to the N queens problem.
// The board is represented by board[1:N], which records
// for each column whether there is a queen in that column,
// and if so, which row it occupies.  In particular,
//   board[j] = i if there is a queen in row i of column j
//            = 0 otherwise

class nQueens {
   static int N = 8;
   static int numSolutions = 0;

   static boolean safe(int row, int column, int[] board) {
//  Check whether it is safe to place a queen at row, column;
//    i.e., is board[column]=row a safe configuration?
      for (int j=1; j<column; j++) {
         if (board[column-j] == row   ||
             board[column-j] == row-j ||
             board[column-j] == row+j) {
            return false;
         }
      }
      return true;
   }

   static void place(int column, int[] board) {
//  Place a queen in all safe positions of column c,
//  then try placing a queen in the next column.
//  If a position in column N is safe, print the board.
      for (int row = 1; row <= N; row++) {
         board[column] = row;
         if (safe(row, column, board)) {
            if (column==N) numSolutions++; // we have a solution
            else place(column+1, board);   // try next column
         }
         board[column] = 0;           // unrecord that a queen was placed
      }
   }

   public static void main(String[] args) {
      System.out.println("nQueens loaded");
      try {          // override default from command line argument if any
         N = Integer.parseInt(args[0]);
      } catch (ArrayIndexOutOfBoundsException e) {
      } catch (NumberFormatException e) {
      }
      System.out.println("nQueens: N=" + N);
      int[] board = new int[N+1];  // N+1 since we need board[1]...board[N]
      place(1, board);
      System.out.println("nQueens: numSolutions=" + numSolutions);
      System.out.println("nQueens done");
   }
}

/* ............... Example compile and run(s)

D:\>javac nque.java

D:\>java nQueens
nQueens loaded
nQueens: N=8
nQueens: numSolutions=92
nQueens done

D:\>java nQueens 10
nQueens loaded
nQueens: N=10
nQueens: numSolutions=724
nQueens done
                                            ... end of example run(s)  */
