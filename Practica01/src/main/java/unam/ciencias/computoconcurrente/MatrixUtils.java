package unam.ciencias.computoconcurrente;

public class MatrixUtils implements Runnable{
    private int threads;
    private static int[] posiblesMinimos; // Arreglo para que cada hilo guarde su minimo encontrado
    private static int[] matrixGlobal; 
    private static int secciones; 

    public MatrixUtils() {
        this.threads = 1;
    }

    public MatrixUtils(int threads) {
        this.threads = threads;
    }

    @Override
    public void run() {
        
    }

    public double findAverage(int[][] matrix) throws InterruptedException{

            return 1.0;
       
    }

    /**
     * Metodo que recorre una matriz de dos dimensiones 
     * @param matrix - matriz de dos dimensiones 
     * @return promedio - promedio de la matriz
     */
    public double Average(int[][] matrix){
        
        return 1.0;

}
