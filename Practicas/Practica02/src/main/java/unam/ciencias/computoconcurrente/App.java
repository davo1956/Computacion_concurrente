package unam.ciencias.computoconcurrente;
import java.util.concurrent.Semaphore;

public class App {

    private static final int NUM_FILOSOFOS = 5;
    private static Semaphore[] tenedores = new Semaphore[NUM_FILOSOFOS];
    private static Semaphore maximoFilosofos = new Semaphore(NUM_FILOSOFOS - 1);
    private static boolean[] comido = new boolean[NUM_FILOSOFOS];


    public static void main(String[] a) throws InterruptedException {
    
        
        for (int i = 0; i < NUM_FILOSOFOS; i++) {
            tenedores[i] = new Semaphore(1);
            comido[i] = false;
        }

        Thread[] filosofos = new Thread[NUM_FILOSOFOS];

        for (int i = 0; i < NUM_FILOSOFOS; i++) {
            final int index = i;
            filosofos[i] = new Thread(() -> {
                while (!comidoTodos()) {
                    try {
                        System.out.println("Filósofo " + index + " está pensando");
                        Thread.sleep((long) (Math.random() * 10000));

                        maximoFilosofos.acquire();
                        tenedores[index].acquire();
                        tenedores[(index + 1) % NUM_FILOSOFOS].acquire();

                        System.out.println("Filósofo " + index + " está comiendo");
                        Thread.sleep((long) (Math.random() * 5000));
                        comido[index] = true;

                        tenedores[index].release();
                        tenedores[(index + 1) % NUM_FILOSOFOS].release();
                        maximoFilosofos.release();

                        System.out.println("Filósofo " + index + " ha terminado de comer");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println("Todos han comido por lo menos una vez");
            });

            filosofos[i].start();
        }
    }

    private static boolean comidoTodos() {
        for (boolean b : comido) {
            if (!b) {
                return false;
            }
        }
        return true;
    }
}
