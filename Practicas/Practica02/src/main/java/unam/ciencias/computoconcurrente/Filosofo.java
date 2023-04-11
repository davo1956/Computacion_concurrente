/**
 * Computación Concurrente
 * Práctica 2 Laboratorio
 * @author Manjarrez Angeles Valeria Fernanda.
 * @author Pérez Jacome David.
*/

package unam.ciencias.computoconcurrente;
import java.util.concurrent.Semaphore;

public class Filosofo implements Runnable {

    private static final Semaphore semaforo = new Semaphore(5);
    public static int DEFAULT_TABLE_SIZE = 5;
    private final int id;
    private final Fork tenedorIzquierdo;
    private final Fork tenedorDerecho;

    public Filosofo(int id, Fork tenedorIzquierdo, Fork tenedorDerecho) {
        this.id = id;
        this.tenedorIzquierdo = tenedorIzquierdo;
        this.tenedorDerecho = tenedorDerecho;
    }

    @Override
    public void run() {
        System.out.println("Filosofo " + id + " esta pensando...");
        try {
            semaforo.acquire();
            tenedorDerecho.take(id);
            System.out.println("Filosofo " + id + " tomó el tenedor Derecho " + tenedorDerecho.id);
            tenedorIzquierdo.take(id);
            semaforo.release();
            System.out.println("Filosofo " + id + " tomó el tenedor Izquierdo " + tenedorIzquierdo.id);
            System.out.println("Filosofo " + id + " está comiendo...");
            tenedorIzquierdo.put(id);
            System.out.println("Filosofo " + id + " devolvio el tenedor Izquierdo " + tenedorIzquierdo.id);
            tenedorDerecho.put(id);
            System.out.println("Filosofo " + id + " devolvio el tenedor Derecho " + tenedorDerecho.id);
        } catch (Exception e) { }
    }

    
}
