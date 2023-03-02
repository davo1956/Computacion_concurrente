/**
 * Computación Concurrente
 * Práctica 1 Laboratorio
 * @author Manjarrez Angeles Valeria Fernanda.
 * @author Pérez Jacome David.
*/
package unam.ciencias.computoconcurrente;
import java.lang.Math;
public class PrimeNumberCalculator implements Runnable{

    private int threads;
    public int idThread;
    private static int numPrimo;
    public static boolean result;
    public static int longitudSubInter; //Dividimos el intervalo [2,N-1] en this.threads cantidad de sub interbalos, uno por cada hilo

    public PrimeNumberCalculator() {
        this.threads = 1;
    }

    public PrimeNumberCalculator(int threads) {
        this.threads = threads > 1 ? threads : 1;
    }
    
    public boolean isPrime(int n) throws InterruptedException{
        if (n == 0 || n == 1)
            return false;

        PrimeNumberCalculator.numPrimo = Math.abs(n);
        PrimeNumberCalculator.longitudSubInter = (int)Math.ceil((PrimeNumberCalculator.numPrimo-2)/threads);
        PrimeNumberCalculator.result = true;
        Thread arrayThread[];
        arrayThread = new Thread[threads];
        for (int i = 1; i <= threads; i++){
            PrimeNumberCalculator obj = new PrimeNumberCalculator();
            obj.idThread = i;
            Thread threadN = new Thread(obj);
            arrayThread[i-1] = threadN;
            threadN.start();
        }
        for (Thread thread : arrayThread) {
            thread.join();
        }
        return result;
    }
    
    @Override
    public void run(){
        for (int i = 2+(idThread-1)*longitudSubInter; i <= 2+(idThread*longitudSubInter)-1; i++) {
            if(i > numPrimo){
                break;
            }
            if (numPrimo % i == 0) {
                PrimeNumberCalculator.result = false;
                break;
            }
        }
    }
}
