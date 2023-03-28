import Utilities.*;
import Synchronization.*;

class DiningServer extends MyObject {

   private static final int
      THINKING = 0, HUNGRY = 1, EATING = 2;
   private int numPhils = 0;
   private int[] state = null;
   private BinarySemaphore[] fork = null;
   private CountingSemaphore room = null;

   public DiningServer(int numPhils) {
      super("DiningServer for " + numPhils + " philosophers");
      this.numPhils = numPhils;
      state = new int[numPhils];
      for (int i = 0; i < numPhils; i++) state[i] = THINKING;
      fork = new BinarySemaphore[numPhils];
      for (int i = 0; i < numPhils; i++) fork[i] = new BinarySemaphore(1);
      room = new CountingSemaphore(numPhils-1);
      System.out.println("Dining room limited to " + (numPhils-1));
   }

   private final int left(int i) { return (numPhils + i - 1) % numPhils; }

   private final int right(int i) { return (i + 1) % numPhils; }

   public void takeForks(int i) {
      state[i] = HUNGRY;
      P(room); P(fork[i]); P(fork[right(i)]);
      state[i] = EATING;
   }

   public void putForks(int i) {
      V(fork[i]); V(fork[right(i)]); V(room);
      state[i] = THINKING;
   }
}

/* ............... Example compile and run(s)

D:\>javac dphf.java dpdr.java

D:\>java DiningPhilosophers -R6 4 1 4 1 4 1 4 1 4 1
DiningPhilosophers: numPhilosophers=5, runTime=6
Dining room limited to 4
Philosopher 0 is alive, napThink=4000, napEat=1000
Philosopher 1 is alive, napThink=4000, napEat=1000
Philosopher 2 is alive, napThink=4000, napEat=1000
Philosopher 3 is alive, napThink=4000, napEat=1000
Philosopher 4 is alive, napThink=4000, napEat=1000
All Philosopher threads started
age()=110, Philosopher 0 is thinking for 3003 ms
age()=110, Philosopher 1 is thinking for 598 ms
age()=110, Philosopher 2 is thinking for 917 ms
age()=110, Philosopher 3 is thinking for 3204 ms
age()=110, Philosopher 4 is thinking for 794 ms
age()=710, Philosopher 1 wants to eat
age()=710, Philosopher 1 is eating for 529 ms
age()=930, Philosopher 4 wants to eat
age()=930, Philosopher 4 is eating for 876 ms
age()=1040, Philosopher 2 wants to eat
age()=1260, Philosopher 1 is thinking for 1583 ms
age()=1260, Philosopher 2 is eating for 696 ms
age()=1810, Philosopher 4 is thinking for 3449 ms
age()=1980, Philosopher 2 is thinking for 1986 ms
age()=2860, Philosopher 1 wants to eat
age()=2860, Philosopher 1 is eating for 921 ms
age()=3130, Philosopher 0 wants to eat
age()=3350, Philosopher 3 wants to eat
age()=3350, Philosopher 3 is eating for 941 ms
age()=3790, Philosopher 1 is thinking for 2125 ms
age()=3790, Philosopher 0 is eating for 985 ms
age()=3950, Philosopher 2 wants to eat
age()=4280, Philosopher 3 is thinking for 114 ms
age()=4280, Philosopher 2 is eating for 537 ms
age()=4390, Philosopher 3 wants to eat
age()=4780, Philosopher 0 is thinking for 3683 ms
age()=4830, Philosopher 2 is thinking for 1179 ms
age()=4830, Philosopher 3 is eating for 139 ms
age()=5000, Philosopher 3 is thinking for 1740 ms
age()=5270, Philosopher 4 wants to eat
age()=5270, Philosopher 4 is eating for 576 ms
age()=5820, Philosopher 4 is thinking for 1004 ms
age()=5880, Philosopher 1 wants to eat
age()=5880, Philosopher 1 is eating for 210 ms
age()=5990, Philosopher 2 wants to eat
age()=6100, time to stop the Philosophers and exit

D:\>java DiningPhilosophers -p8 1 10000 263 10 131 10 65 10 31 10 15 10 7 10 3 10
DiningPhilosophers: numPhilosophers=8, runTime=60
Dining room limited to 7
Philosopher 0 is alive, napThink=1000, napEat=10000000
Philosopher 1 is alive, napThink=263000, napEat=10000
Philosopher 2 is alive, napThink=131000, napEat=10000
Philosopher 3 is alive, napThink=65000, napEat=10000
Philosopher 4 is alive, napThink=31000, napEat=10000
Philosopher 5 is alive, napThink=15000, napEat=10000
Philosopher 6 is alive, napThink=7000, napEat=10000
Philosopher 7 is alive, napThink=3000, napEat=10000
All Philosopher threads started
age()=110, Philosopher 0 is thinking for 910 ms
age()=110, Philosopher 1 is thinking for 246148 ms
age()=170, Philosopher 2 is thinking for 63677 ms
age()=170, Philosopher 3 is thinking for 47525 ms
age()=220, Philosopher 4 is thinking for 20629 ms
age()=220, Philosopher 5 is thinking for 7549 ms
age()=220, Philosopher 6 is thinking for 5058 ms
age()=220, Philosopher 7 is thinking for 2274 ms
age()=1040, Philosopher 0 wants to eat
age()=1040, Philosopher 0 is eating for 7382505 ms
age()=2640, Philosopher 7 wants to eat
age()=5380, Philosopher 6 wants to eat
age()=7850, Philosopher 5 wants to eat
age()=20930, Philosopher 4 wants to eat
age()=47790, Philosopher 3 wants to eat
age()=60090, time to stop the Philosophers and exit
                                            ... end of example run(s)  */
