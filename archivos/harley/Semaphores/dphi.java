import Utilities.*;
import Synchronization.*;

class DiningServer extends MyObject {

   private static final int
      THINKING = 0, HUNGRY = 1, EATING = 2;
   private int numPhils = 0;
   private int[] state = null;
   private BinarySemaphore[] self = null;
   private BinarySemaphore mutex = null;

   public DiningServer(int numPhils) {
      super("DiningServer for " + numPhils + " philosophers");
      this.numPhils = numPhils;
      state = new int[numPhils];
      for (int i = 0; i < numPhils; i++) state[i] = THINKING;
      self = new BinarySemaphore[numPhils];
      for (int i = 0; i < numPhils; i++) self[i] = new BinarySemaphore(0);
      mutex = new BinarySemaphore(1);
   }

   private final int left(int i) { return (numPhils + i - 1) % numPhils; }

   private final int right(int i) { return (i + 1) % numPhils; }

   public void takeForks(int i) {
      P(mutex);
      state[i] = HUNGRY;
      test(i);
      V(mutex);
      P(self[i]);
   }

   public void putForks(int i) {
      P(mutex);
      state[i] = THINKING;
      test(left(i));
      test(right(i));
      V(mutex);
   }

   private void test(int k) {
      if (state[left(k)] != EATING && state[right(k)] != EATING
            && state[k] == HUNGRY) {
         state[k] = EATING;
         V(self[k]);
      }
   }
}

/* ............... Example compile and run(s)

D:\>javac dphi.java dpdr.java

D:\>java DiningPhilosophers -R10 4 1 4 1 4 1 4 1 4 1
DiningPhilosophers: numPhilosophers=5, runTime=10
Philosopher 0 is alive, napThink=4000, napEat=1000
Philosopher 1 is alive, napThink=4000, napEat=1000
Philosopher 2 is alive, napThink=4000, napEat=1000
Philosopher 3 is alive, napThink=4000, napEat=1000
Philosopher 4 is alive, napThink=4000, napEat=1000
All Philosopher threads started
age()=110, Philosopher 0 is thinking for 3816 ms
age()=110, Philosopher 1 is thinking for 2079 ms
age()=110, Philosopher 2 is thinking for 2646 ms
age()=170, Philosopher 3 is thinking for 560 ms
age()=170, Philosopher 4 is thinking for 761 ms
age()=720, Philosopher 3 wants to eat
age()=720, Philosopher 3 is eating for 717 ms
age()=940, Philosopher 4 wants to eat
age()=1430, Philosopher 3 is thinking for 1653 ms
age()=1430, Philosopher 4 is eating for 913 ms
age()=2200, Philosopher 1 wants to eat
age()=2200, Philosopher 1 is eating for 86 ms
age()=2310, Philosopher 1 is thinking for 1797 ms
age()=2370, Philosopher 4 is thinking for 3052 ms
age()=2810, Philosopher 2 wants to eat
age()=2810, Philosopher 2 is eating for 442 ms
age()=3140, Philosopher 3 wants to eat
age()=3300, Philosopher 2 is thinking for 3003 ms
age()=3300, Philosopher 3 is eating for 465 ms
age()=3740, Philosopher 3 is thinking for 1381 ms
age()=3960, Philosopher 0 wants to eat
age()=3960, Philosopher 0 is eating for 247 ms
age()=4120, Philosopher 1 wants to eat
age()=4180, Philosopher 0 is thinking for 1642 ms
age()=4180, Philosopher 1 is eating for 787 ms
age()=5000, Philosopher 1 is thinking for 2828 ms
age()=5170, Philosopher 3 wants to eat
age()=5170, Philosopher 3 is eating for 559 ms
age()=5440, Philosopher 4 wants to eat
age()=5720, Philosopher 3 is thinking for 319 ms
age()=5720, Philosopher 4 is eating for 850 ms
age()=5830, Philosopher 0 wants to eat
age()=6050, Philosopher 3 wants to eat
age()=6270, Philosopher 2 wants to eat
age()=6270, Philosopher 2 is eating for 33 ms
age()=6320, Philosopher 2 is thinking for 92 ms
age()=6430, Philosopher 2 wants to eat
age()=6430, Philosopher 2 is eating for 612 ms
age()=6600, Philosopher 4 is thinking for 3242 ms
age()=6600, Philosopher 0 is eating for 320 ms
age()=6930, Philosopher 0 is thinking for 3193 ms
age()=7040, Philosopher 2 is thinking for 3221 ms
age()=7040, Philosopher 3 is eating for 142 ms
age()=7200, Philosopher 3 is thinking for 2902 ms
age()=7800, Philosopher 1 wants to eat
age()=7800, Philosopher 1 is eating for 553 ms
age()=8410, Philosopher 1 is thinking for 3382 ms
age()=9840, Philosopher 4 wants to eat
age()=9840, Philosopher 4 is eating for 678 ms
age()=10110, Philosopher 3 wants to eat
age()=10110, Philosopher 0 wants to eat
age()=10110, time to stop the Philosophers and exit
                                            ... end of example run(s)  */
