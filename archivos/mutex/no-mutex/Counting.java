// Ejemplo hecho para ilustrar la mutabilidad del estado compartido de los hilos.
// Muestra un resultado distinto cada vez que se corre el programa

public class Counting {
  public static void main(String[] args) throws InterruptedException {
  class Counter {
  int counter = 0;
  public void increment() { counter++; }
  public int get() { return counter; }
  }
  final Counter counter = new Counter();

  class CountingThread extends Thread {
  public void run() {
  for (int x = 0; x < 500000; x++) {
  counter.increment();
      }
    }
  }

  CountingThread t1 = new CountingThread();
  CountingThread t2 = new CountingThread();
  t1.start(); t2.start();
  t1.join(); t2.join();
  System.out.println(counter.get());//deberia dar 1000000
  }
}