/**
 * Computación Concurrente
 * Práctica 2 Laboratorio
 * @author Manjarrez Angeles Valeria Fernanda.
 * @author Pérez Jacome David.
*/

package unam.ciencias.computoconcurrente;

public class App {

    public static void main(String[] a) throws InterruptedException {

        GestorBD gestor=new GestorBD();
        Escritor[] escritores=new Escritor[5];
        Lector[] lectores=new Lector[5];

        for(int i=0;i<escritores.length;i++){
            escritores[i] = new Escritor(gestor, i);
        }

        for(int i=0;i<lectores.length;i++){
            lectores[i] = new Lector(gestor, i);
        }

        //inicializn lectores y escritores
        
        for(int i=0;i<escritores.length;i++){
            escritores[i].start();
        }

        for(int i=0;i<lectores.length;i++){
            lectores[i].start();
        }


    }
}
