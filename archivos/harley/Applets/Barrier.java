// a Barrier for m*n threads: all must arrive before any are released
public class Barrier extends MyObject implements Runnable {

   private int m = -1, n = -1;
   private CountingSemaphore arrive = null;
   private BinarySemaphore[][] release = null;
   private Thread thread = null;

   public Barrier(int m, int n) {
      super("Barrier for m=" + m + " n=" + n + " threads");
      this.m = m;
      this.n = n;
      arrive = new CountingSemaphore(0);
      release = new BinarySemaphore[m][n];
      for (int i = 0; i < m; i++) for (int j = 0; j < n; j++)
         release[i][j] = new BinarySemaphore(0);
      thread = new Thread(this);
      thread.setDaemon(true);
      thread.start();
   }

   public Barrier(int n) { this(1, n); }

   public String toString() { return getName() + ", arrive=" + arrive; }

   public void gate(int i, int j) {
      V(arrive);
      P(release[i][j]);
   }

   public void gate(int j) { this.gate(0, j); }

   // not really needed since this is a daemon thread
   public void stop() { thread.stop(); }

   public void run() {
      while (true) {
// If we are worried about two different threads mistakenly
// calling barrier(i) for the same i, we can make arrive into
// an array of semaphores like semaphore release already is.
         for (int i = 0; i < m; i++) for (int j = 0; j < n; j++)
            P(arrive);
// Second semaphore must be an array to avoid race conditions
// due to arbitrary context switches and relative CPU speeds.
         for (int i = 0; i < m; i++) for (int j = 0; j < n; j++)
            V(release[i][j]);
      }
   }
}
