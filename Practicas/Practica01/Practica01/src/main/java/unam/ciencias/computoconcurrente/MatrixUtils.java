/**
 * Computación Concurrente
 * Práctica 1 Laboratorio
 * @author Manjarrez Angeles Valeria Fernanda.
 * @author Pérez Jacome David.
*/
package unam.ciencias.computoconcurrente;
public class MatrixUtils implements Runnable{

    private int threads;
    public int idThread;
    public static int total;
    private static int[] suma;
    private static int[][] matrixGlobal;
    private static int longitudSubInter; 

    public MatrixUtils() {
        this.threads = 1;
    }

    public MatrixUtils(int threads) {
        this.threads = threads > 1 ? threads : 1;
    }

    public double findAverage(int[][] matrix) throws InterruptedException{
        MatrixUtils.total = matrix.length * matrix[0].length;
        MatrixUtils.matrixGlobal = matrix;
        MatrixUtils.longitudSubInter = (int)Math.floor(total/threads);
        int intervalos = total / longitudSubInter;

        if (longitudSubInter*intervalos == total) {
            MatrixUtils.suma = new int[intervalos];
        } else {
            MatrixUtils.suma = new int[intervalos+1];
        }

        Thread arrayThread[];
        arrayThread = new Thread[threads];
        for (int i = 0; i < threads; i++){
            MatrixUtils obj = new MatrixUtils();
            obj.idThread = i;
            Thread threadN = new Thread(obj);
            arrayThread[i] = threadN;
            threadN.start();
        }
        for (Thread thread : arrayThread) {
            thread.join();
        }
        Thread arrayThreadExtra[];
        arrayThreadExtra = new Thread[threads];
        for (int i = threads; i < MatrixUtils.suma.length; i++){
            MatrixUtils obj = new MatrixUtils();
            obj.idThread = i;
            Thread threadN = new Thread(obj);
            arrayThreadExtra[i-threads] = threadN;
            threadN.start();
        }
        for (Thread thread : arrayThreadExtra) {
            if (thread != null)
                thread.join();
        }
        int totalS = 0;
        for (int i = 0; i < suma.length; i++){
            totalS += suma[i];
        }
        double result = (double)totalS/total;
            return result;
    }

    @Override
    public void run() {
        suma[idThread] = 0;

        for (int i = idThread*longitudSubInter; i < Math.min((idThread+1)*longitudSubInter, total); i++){
            int fila = (int)Math.floor(i/matrixGlobal[0].length);
            int columna = i - (fila*matrixGlobal[0].length);
            int sumaI = matrixGlobal[fila][columna];
            suma[idThread] += sumaI;
        }
    }

    /**
     * Metodo que recorre una matriz de dos dimensiones 
     * @param matrix - matriz de dos dimensiones 
     * @return promedio - promedio de la matriz
     */
    public double Average(int[][] matrix){
        
        return 1.0;
    }
}
