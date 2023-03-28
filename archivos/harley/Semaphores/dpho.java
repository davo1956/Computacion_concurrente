import Utilities.*;
import Synchronization.*;

class DiningServer extends MyObject {

   private static final int
      THINKING = 0, HUNGRY = 1, EATING = 2;
   private int numPhils = 0;
   private int[] state = null;
   private BinarySemaphore[] fork = null;

   public DiningServer(int numPhils) {
      super("DiningServer for " + numPhils + " philosophers");
      this.numPhils = numPhils;
      state = new int[numPhils];
      for (int i = 0; i < numPhils; i++) state[i] = THINKING;
      fork = new BinarySemaphore[numPhils];
      for (int i = 0; i < numPhils; i++) fork[i] = new BinarySemaphore(1);
      System.out.println("Dining room has an `odd' eater");
   }

   private final int left(int i) { return (numPhils + i - 1) % numPhils; }

   private final int right(int i) { return (i + 1) % numPhils; }

   public void takeForks(int i) {
      state[i] = HUNGRY;
      if (i > 0) { P(fork[i]); P(fork[right(i)]); }
      else       { P(fork[right(i)]); P(fork[i]); }
      state[i] = EATING;
   }

   public void putForks(int i) {
      V(fork[i]); V(fork[right(i)]);
      state[i] = THINKING;
   }
}

/* ............... Example compile and run(s)

D:\>javac dpho.java dpdr.java

D:\>java DiningPhilosophers -R6 4 1 4 1 4 1 4 1 4 1
DiningPhilosophers: numPhilosophers=5, runTime=6
Dining room has an `odd' eater
Philosopher 0 is alive, napThink=4000, napEat=1000
Philosopher 1 is alive, napThink=4000, napEat=1000
Philosopher 2 is alive, napThink=4000, napEat=1000
Philosopher 3 is alive, napThink=4000, napEat=1000
Philosopher 4 is alive, napThink=4000, napEat=1000
All Philosopher threads started
age()=110, Philosopher 0 is thinking for 2706 ms
age()=110, Philosopher 1 is thinking for 2385 ms
age()=110, Philosopher 2 is thinking for 1094 ms
age()=110, Philosopher 3 is thinking for 701 ms
age()=110, Philosopher 4 is thinking for 1094 ms
age()=820, Philosopher 3 wants to eat
age()=820, Philosopher 3 is eating for 197 ms
age()=1040, Philosopher 3 is thinking for 2369 ms
age()=1210, Philosopher 2 wants to eat
age()=1210, Philosopher 2 is eating for 444 ms
age()=1210, Philosopher 4 wants to eat
age()=1210, Philosopher 4 is eating for 51 ms
age()=1320, Philosopher 4 is thinking for 3931 ms
age()=1700, Philosopher 2 is thinking for 2555 ms
age()=2530, Philosopher 1 wants to eat
age()=2530, Philosopher 1 is eating for 25 ms
age()=2580, Philosopher 1 is thinking for 3874 ms
age()=2860, Philosopher 0 wants to eat
age()=2860, Philosopher 0 is eating for 487 ms
age()=3350, Philosopher 0 is thinking for 531 ms
age()=3410, Philosopher 3 wants to eat
age()=3410, Philosopher 3 is eating for 29 ms
age()=3460, Philosopher 3 is thinking for 3184 ms
age()=3850, Philosopher 0 wants to eat
age()=3850, Philosopher 0 is eating for 161 ms
age()=4010, Philosopher 0 is thinking for 902 ms
age()=4230, Philosopher 2 wants to eat
age()=4230, Philosopher 2 is eating for 460 ms
age()=4720, Philosopher 2 is thinking for 2406 ms
age()=4940, Philosopher 0 wants to eat
age()=4940, Philosopher 0 is eating for 804 ms
age()=5220, Philosopher 4 wants to eat
age()=5770, Philosopher 0 is thinking for 1013 ms
age()=5770, Philosopher 4 is eating for 927 ms
age()=6100, time to stop the Philosophers and exit

D:\>java DiningPhilosophers -p8 1 10000 263 10 131 10 65 10 31 10 15 10 7 10 3 10
DiningPhilosophers: numPhilosophers=8, runTime=60
Dining room has an `odd' eater
Philosopher 0 is alive, napThink=1000, napEat=10000000
Philosopher 1 is alive, napThink=263000, napEat=10000
Philosopher 2 is alive, napThink=131000, napEat=10000
Philosopher 3 is alive, napThink=65000, napEat=10000
Philosopher 4 is alive, napThink=31000, napEat=10000
Philosopher 5 is alive, napThink=15000, napEat=10000
Philosopher 6 is alive, napThink=7000, napEat=10000
Philosopher 7 is alive, napThink=3000, napEat=10000
All Philosopher threads started
age()=110, Philosopher 0 is thinking for 244 ms
age()=110, Philosopher 1 is thinking for 54397 ms
age()=110, Philosopher 2 is thinking for 65343 ms
age()=110, Philosopher 3 is thinking for 64326 ms
age()=110, Philosopher 4 is thinking for 18774 ms
age()=110, Philosopher 5 is thinking for 9341 ms
age()=170, Philosopher 6 is thinking for 3000 ms
age()=170, Philosopher 7 is thinking for 2040 ms
age()=390, Philosopher 0 wants to eat
age()=390, Philosopher 0 is eating for 7455433 ms
age()=2200, Philosopher 7 wants to eat
age()=3190, Philosopher 6 wants to eat
age()=9500, Philosopher 5 wants to eat
age()=18890, Philosopher 4 wants to eat
age()=54540, Philosopher 1 wants to eat
age()=60090, time to stop the Philosophers and exit
                                            ... end of example run(s)  */
