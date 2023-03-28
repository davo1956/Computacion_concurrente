package unam.ciencias.computoconcurrente;

/**
 *  Cada filósofo se ejecuta en un hilo.
 */
public abstract class Filosofo implements Runnable {
    public static int DEFAULT_TABLE_SIZE = 5;

    protected int id;
}