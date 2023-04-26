/**
 * Computación Concurrente
 * Práctica 2 Laboratorio
 * @author Manjarrez Angeles Valeria Fernanda.
 * @author Pérez Jacome David.
*/

package unam.ciencias.computoconcurrente;
import java.util.*;

/**
 *  Clase de Lector y sera un hilo.
 */

public class Lector extends Thread{
    
    private static Random r = new Random();
    private GestorBD gestor;
    private int id; //identificador de lector

    public Lector(GestorBD gestor, int id){
        this.gestor = gestor;
        this.id = id;
    }

    /**
     * Metodo que se ejecuta siempre en un lector
     */

    public void run() {
        while(true){
            try {
                gestor.abrirLector(id);
                //leyendo la bd
                Thread.sleep(r.nextInt(200));
                gestor.cerrarLector(id);
                Thread.sleep(r.nextInt(300));
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        
    }


}
