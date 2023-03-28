import Utilities.*;

class Matrix {
   private int[][] matrix = null;
   private int numRows = 0, numCols = 0;
   public Matrix(int numRows, int numCols) {
      this.numRows = numRows; this.numCols = numCols;
      matrix = new int[numRows][numCols];
   }
   public int getEntry(int i, int j) { return matrix[i][j]; }
   public void setEntry(int i, int j, int e) { matrix[i][j] = e; }
   public int diagUp(int row) { return (row + 1) % numRows; }
   public int diagDown(int row) { return (numRows + row - 1) % numRows; }
}

class Calculate extends MyObject implements Runnable {
   private int row = 0;
   private Matrix a = null;
   private Thread t = null;
   public Calculate(int row, Matrix a) {
      this.row = row; this.a = a;
      t = new Thread(this);
      t.start();
   }
   public void run() {
      int minimumWeight = 0;
      System.out.println("Calculate thead for row " + row);
      // calculate backwards
      System.out.println("The minimum weight starting on the left in row "
         + row + " is " + minimumWeight);
   }
}

class Driver {
   public static void main(String[] args) {
      int m = Integer.parseInt(args[0]);
      int n = Integer.parseInt(args[1]);
      Matrix a = new Matrix(m, n);
      // fill in the matrix from args
      for (int i = 0; i < m; i++)
         Calculate c = new Calculate(i, a);
   }
}
