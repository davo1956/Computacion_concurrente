/**
 * Computación Concurrente
 * Práctica 2 Laboratorio
 * @author Manjarrez Angeles Valeria Fernanda.
 * @author Pérez Jacome David.
*/

package unam.ciencias.computoconcurrente;

/**
 *  Clase del gestor de la base de datos con los metodos de contrl de lectores y escritores
 */

public class GestorBD {

    private int numeroLectores=0;
    private boolean hayEscritor=false;
    private int numeroEscritores=0;


    /**
     * Metodo que abre a lectores al ser synchronized se hace un monitor
     */

    public synchronized void abrirLector(int id) throws InterruptedException{
        while(hayEscritor || numeroEscritores>0){
            wait();
        }
        numeroLectores++;
        System.out.println("Lector " +id+ " entra a la base de datos");
    }

    /**
     * Metodo que cierra o saca a los lectores
     */

    public synchronized void cerrarLector(int id){
        System.out.println("Lector " +id+ " sale de la base de datos");
        numeroLectores--;
        if(numeroLectores == 0) notifyAll();
    }

    

    /**
     * Metodo que abre o mete a los escritores
     */

    public synchronized void abrirEscritor(int id) throws InterruptedException{
        numeroEscritores++;
        while(hayEscritor || numeroLectores>0){
            wait();
        }
        hayEscritor = true;
        System.out.println("Escritor " +id+ " entra a la base de datos");
    }

    /**
     * Metodo que cierra o saca a los escritores
     */

    public synchronized void cerrarEscritor(int id) {
        numeroEscritores--;
        System.out.println("Escritor " +id+ " sale de la base de datos");
        hayEscritor=false;
        notifyAll(); //le dice a todos que pueden entrar cualquiera.
    }
    
}
