/**
 * Computación Concurrente
 * Práctica 2 Laboratorio
 * @author Manjarrez Angeles Valeria Fernanda.
 * @author Pérez Jacome David.
*/

package unam.ciencias.computoconcurrente;

public class Fork {

    public final int id;
    private boolean tenedoresEnMesa = true;
    private int filosofoConTenedor;

    public Fork(int id) {
        this.id = id;
    }

    public synchronized void take(int filosofo) {
        while (!tenedoresEnMesa) {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }
        filosofoConTenedor = filosofo;
        tenedoresEnMesa = false;
    }

    public synchronized void put(int filosofo) {
        if (!tenedoresEnMesa && filosofoConTenedor == filosofo) {
            tenedoresEnMesa = true;
            notify();
        }
    }
    
}
