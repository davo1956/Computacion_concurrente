/**
 * Computación Concurrente
 * Práctica 2 Laboratorio
 * @author Manjarrez Angeles Valeria Fernanda.
 * @author Pérez Jacome David.
*/
package unam.ciencias.computoconcurrente;


public class App {

    public static Filosofo[] mesaFilosofos(int n) {
        Fork[] tenedores = new Fork[n];
        for (int i = 0; i < n; i++) {
            tenedores[i] = new Fork(i);
        }
        Filosofo[] filosofos = new Filosofo[n];
        for (int i = 0; i < n; i++) {
            Fork tenedorIzquierdo = tenedores[i];
            Fork tenedorDerecho = tenedores[(i + 1) % n];
            filosofos[i] = new Filosofo(i, tenedorIzquierdo, tenedorDerecho);
        }
        return filosofos;
    }

    public static void main(String[] a) throws InterruptedException {
        Filosofo[] filosofoPrueba = mesaFilosofos(5);
        Thread arrayThread[];
        arrayThread = new Thread[5];
        for (int i = 0; i < 5; i++){
            Thread threadN = new Thread(filosofoPrueba[i]);
            arrayThread[i] = threadN;
            threadN.start();
        }
        for (Thread thread : arrayThread) {
            thread.join();
        }
    }

}
