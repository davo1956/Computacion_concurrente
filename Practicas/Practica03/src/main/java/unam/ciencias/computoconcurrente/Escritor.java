/**
 * Computación Concurrente
 * Práctica 2 Laboratorio
 * @author Manjarrez Angeles Valeria Fernanda.
 * @author Pérez Jacome David.
*/

package unam.ciencias.computoconcurrente;
import java.util.*;

/**
 *  Clase de Escritor y sera un hilo.
 */


public class Escritor extends Thread{
    
    private static Random r = new Random();
    private GestorBD gestor;
    private int id; //identificador de escritor

    public Escritor(GestorBD gestor, int id){
        this.gestor = gestor;
        this.id = id;
    }

    /**
     * Metodo que se ejecuta siempre en un escritor para entrar a la base de datos.
     */

    public void run(){
        while(true){
            try {
                gestor.abrirEscritor(id);
                //escribir en  la bd
                Thread.sleep(r.nextInt(200));
                gestor.cerrarEscritor(id);
                Thread.sleep(r.nextInt(300));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
    }


}
