class Cell {
   private int cellState = 1;

   Cell() { if (Math.random() > 0.5) cellState = 1; else cellState = 0; }

   Cell(int state)   { cellState = state; }

   public void setCellState(int state) { cellState = state; }

   public int getCellState() { return cellState; }
}

class Grid {
   int numRows, numColumns;
   Cell[][] cellMatrix;

   Grid(int numRows, int numColumns)  {
      this.numRows = numRows;
      this.numColumns = numColumns;
      cellMatrix = new Cell[numRows][numColumns];
      for(int i = 0; i < numRows; i++) for(int j = 0; j < numColumns; j++)
         cellMatrix[i][j] = new Cell();
   }

   public int getGridCellState(int row, int column) {
      return cellMatrix[row][column].getCellState();
   }
   
   public void setGridCellState(int row, int column, int cellState) {
      cellMatrix[row][column].setCellState(cellState);
   }

   public void displayGrid() {
      for(int i = 0; i < numRows; i++) {
         for(int j = 0; j < numColumns; j++)
            System.out.print(cellMatrix[i][j].getCellState() + " ");
         System.out.println();
      }
   }
}

class Life {

   public static void main(String[] args) {
      int numRows = 5, numColumns = 5, numGenerations = 5;

      try {
         numRows = Integer.parseInt(args[0]);
         numColumns = Integer.parseInt(args[1]);
         numGenerations = Integer.parseInt(args[2]);
      } catch (Exception e) {}

      Grid grid = new Grid(numRows, numColumns);

      for (int i = 0; i < numRows; i++) for (int j = 0; j < numColumns; j++)
         try {
            grid.setGridCellState(i, j,
               Integer.parseInt(args[3+i*numColumns+j]));
         } catch (Exception e) {}

      System.out.println("Initial population");
      grid.displayGrid();

      for (int i = 0; i < numRows; i++) for (int j = 0; j < numColumns; j++)
        new CellThread(i, j, numGenerations, grid);
   }
}

class CellThread implements Runnable {
    int i, j, numGenerations;
    Grid grid;

    CellThread(int i, int j, int numGenerations, Grid grid) {
       this.i = i; this.j = j; this.numGenerations = numGenerations;
       this.grid = grid;
       new Thread(this).start();
    }

    public void run() {

      for(int n = 0; n < numGenerations; n++) {
         if (i==0&&j==0) System.out.println("Generation # " + (n+1));
         int upNeighbor, downNeighbor, leftNeighbor, rightNeighbor;
         int upLeftNeighbor, upRightNeighbor, downLeftNeighbor,
            downRightNeighbor;
         int totalAliveNeighbors;
         int newGenValue;

         upNeighbor = 0; downNeighbor = 0; leftNeighbor = 0; rightNeighbor = 0;
         upLeftNeighbor = 0; upRightNeighbor = 0; downLeftNeighbor = 0;
         downRightNeighbor = 0;
         totalAliveNeighbors = 0;

         // Upper left corner
         if((i==0) && (j==0)) {
            rightNeighbor = grid.getGridCellState(i, j+1);
            downRightNeighbor = grid.getGridCellState(i+1, j+1);
            downNeighbor = grid.getGridCellState(i+1, j);
         }

         // Upper right corner
         if((i==0) && (j==grid.numColumns-1)) {
            downNeighbor = grid.getGridCellState(i+1, j);
            downLeftNeighbor = grid.getGridCellState(i+1, j-1);
            leftNeighbor = grid.getGridCellState(i, j-1);
         }

         // Down right corner
         if((i==grid.numRows-1) && (j==grid.numColumns-1)) {
            leftNeighbor = grid.getGridCellState(i, j-1);
            upLeftNeighbor = grid.getGridCellState(i-1, j-1);
            upNeighbor = grid.getGridCellState(i-1, j);
         }

         // Down left corner
         if((i==grid.numRows-1) && (j==0)) {
            upNeighbor = grid.getGridCellState(i-1, j);
            upRightNeighbor = grid.getGridCellState(i-1, j+1);
            rightNeighbor = grid.getGridCellState(i, j+1);
         }

         // Top row
         if((i==0) && (j!=0) && (j!=grid.numColumns -1)) {
            rightNeighbor = grid.getGridCellState(i, j+1);
            leftNeighbor = grid.getGridCellState(i, j-1);
            downNeighbor = grid.getGridCellState(i+1, j);
            downLeftNeighbor = grid.getGridCellState(i+1, j-1);
            downRightNeighbor = grid.getGridCellState(i+1, j+1);
         }

         // Bottom row
         if((i==grid.numRows-1) && (j!=0) && (j!=grid.numColumns -1)) {
            rightNeighbor = grid.getGridCellState(i, j+1);
            leftNeighbor = grid.getGridCellState(i, j-1);
            upNeighbor = grid.getGridCellState(i-1, j);
            upLeftNeighbor = grid.getGridCellState(i-1, j-1);
            upRightNeighbor = grid.getGridCellState(i-1, j+1);
         }

         // Left column
         if((i!=0) && (i!=grid.numRows-1) && (j==0)) {
            upNeighbor = grid.getGridCellState(i-1, j);
            downNeighbor = grid.getGridCellState(i+1, j);
            rightNeighbor = grid.getGridCellState(i, j+1);
            upRightNeighbor = grid.getGridCellState(i-1, j+1);
            downRightNeighbor = grid.getGridCellState(i+1, j+1);
         }

         // Right column
         if((i!=0) && (i!=grid.numRows-1) && (j==grid.numColumns-1)) {
            upNeighbor = grid.getGridCellState(i-1, j);
            downNeighbor = grid.getGridCellState(i+1, j);
            leftNeighbor = grid.getGridCellState(i, j-1);
            upLeftNeighbor = grid.getGridCellState(i-1, j-1);
            downLeftNeighbor = grid.getGridCellState(i+1, j-1);
         }

         // Rest of the grid cells
         if((i!=0) && (i!=grid.numRows-1) && (j!=0) && (j!=grid.numColumns-1)) {
            upNeighbor = grid.getGridCellState(i-1, j);
            downNeighbor = grid.getGridCellState(i+1, j);
            leftNeighbor = grid.getGridCellState(i, j-1);
            rightNeighbor = grid.getGridCellState(i, j+1);
            upLeftNeighbor = grid.getGridCellState(i-1, j-1);
            upRightNeighbor = grid.getGridCellState(i-1, j+1);
            downLeftNeighbor = grid.getGridCellState(i+1, j-1);
            downRightNeighbor = grid.getGridCellState(i+1, j+1);
         }

         totalAliveNeighbors = upNeighbor;
         totalAliveNeighbors += downNeighbor;
         totalAliveNeighbors += leftNeighbor;
         totalAliveNeighbors += rightNeighbor;
         totalAliveNeighbors += upLeftNeighbor;
         totalAliveNeighbors += upRightNeighbor;
         totalAliveNeighbors += downLeftNeighbor;
         totalAliveNeighbors += downRightNeighbor;

         if(grid.getGridCellState(i, j)==1) {
            if((totalAliveNeighbors==2) || (totalAliveNeighbors==3))
               newGenValue = grid.getGridCellState(i, j);
            else newGenValue = 0;
         } else {
            if(totalAliveNeighbors==3) newGenValue = 1;
            else newGenValue = grid.getGridCellState(i, j);
         }

System.out.println("i=" + i + " j=" + j + " n=" + n + " old="
   + grid.getGridCellState(i, j) + " total=" + totalAliveNeighbors
   + " new=" + newGenValue);

         grid.setGridCellState(i, j, newGenValue);
         if (i==0&&j==0) grid.displayGrid();
      }
   }
}
