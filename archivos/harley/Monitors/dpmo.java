class DiningServer {

   private boolean checkStarving = false;
   private int numPhils = 0;
   private int[] state = null;
   private static final int
      THINKING = 0, HUNGRY = 1, STARVING = 2, EATING = 3;

   public DiningServer(int numPhils, boolean checkStarving) {
      this.numPhils = numPhils;
      this.checkStarving = checkStarving;
      state = new int[numPhils];
      for (int i = 0; i < numPhils; i++) state[i] = THINKING;
      System.out.println("DiningServer: checkStarving="
         + checkStarving);
   }

   private final int left(int i) { return (numPhils + i - 1) % numPhils; }

   private final int right(int i) { return (i + 1) % numPhils; }

   private void seeIfStarving(int k) {
      if (state[k] == HUNGRY && state[left(k)] != STARVING &&
            state[right(k)] != STARVING) {
         state[k] = STARVING;
         System.out.println("philosopher " + k + " is STARVING");
      }
   }

   private void test(int k, boolean checkStarving) {
      if (state[left(k)] != EATING && state[left(k)] != STARVING &&
            (state[k] == HUNGRY || state[k] == STARVING) &&
            state[right(k)] != STARVING && state[right(k)] != EATING)
         state[k] = EATING;
      else if (checkStarving)
         seeIfStarving(k); // simplistic naive check for starvation
   }

   public synchronized void takeForks(int i) {
      state[i] = HUNGRY;
      test(i, false);
      while (state[i] != EATING)
         try {wait();} catch (InterruptedException e) {}
   }

   public synchronized void putForks(int i) {
      state[i] = THINKING;
      test(left(i), checkStarving);
      test(right(i), checkStarving);
      notifyAll();
   }
}

/* ............... Example compile and run(s)

D:\>javac dpmo.java dpdr.java

D:\>java DiningPhilosophers -R120 1 100 10 1 1 100 1000000 1 1000000 1
DiningPhilosophers: numPhilosophers=5, checkStarving=false, runTime=120
DiningServer: checkStarving=false
Philosopher 0 is alive, napThink=1000, napEat=100000
Philosopher 1 is alive, napThink=10000, napEat=1000
Philosopher 2 is alive, napThink=1000, napEat=100000
Philosopher 3 is alive, napThink=1000000000, napEat=1000
Philosopher 4 is alive, napThink=1000000000, napEat=1000
All Philosopher threads started
age()=110, Philosopher 0 is thinking for 879 ms
age()=110, Philosopher 1 is thinking for 4468 ms
age()=110, Philosopher 2 is thinking for 191 ms
age()=110, Philosopher 3 is thinking for 278615928 ms
age()=170, Philosopher 4 is thinking for 504256757 ms
age()=330, Philosopher 2 wants to eat
age()=330, Philosopher 2 is eating for 91417 ms
age()=990, Philosopher 0 wants to eat
age()=990, Philosopher 0 is eating for 41245 ms
age()=4620, Philosopher 1 wants to eat
age()=42460, Philosopher 0 is thinking for 688 ms
age()=43170, Philosopher 0 wants to eat
age()=43170, Philosopher 0 is eating for 5407 ms
age()=48560, Philosopher 0 is thinking for 342 ms
age()=48890, Philosopher 0 wants to eat
age()=48890, Philosopher 0 is eating for 70762 ms
age()=91730, Philosopher 2 is thinking for 737 ms
age()=92500, Philosopher 2 wants to eat
age()=92500, Philosopher 2 is eating for 22323 ms
age()=114800, Philosopher 2 is thinking for 204 ms
age()=115020, Philosopher 2 wants to eat
age()=115020, Philosopher 2 is eating for 56934 ms
age()=119680, Philosopher 0 is thinking for 401 ms
age()=120070, Philosopher 0 wants to eat
age()=120070, Philosopher 0 is eating for 58416 ms
age()=120070, time to stop the Philosophers and exit

D:\>java DiningPhilosophers -S -R120 1 100 10 1 1 100 1000000 1 1000000 1
DiningPhilosophers: numPhilosophers=5, checkStarving=true, runTime=120
DiningServer: checkStarving=true
Philosopher 0 is alive, napThink=1000, napEat=100000
Philosopher 1 is alive, napThink=10000, napEat=1000
Philosopher 2 is alive, napThink=1000, napEat=100000
Philosopher 3 is alive, napThink=1000000000, napEat=1000
Philosopher 4 is alive, napThink=1000000000, napEat=1000
All Philosopher threads started
age()=110, Philosopher 0 is thinking for 623 ms
age()=170, Philosopher 1 is thinking for 739 ms
age()=170, Philosopher 2 is thinking for 304 ms
age()=220, Philosopher 3 is thinking for 625066794 ms
age()=220, Philosopher 4 is thinking for 852766912 ms
age()=550, Philosopher 2 wants to eat
age()=550, Philosopher 2 is eating for 92594 ms
age()=880, Philosopher 0 wants to eat
age()=880, Philosopher 0 is eating for 30529 ms
age()=990, Philosopher 1 wants to eat
philosopher 1 is STARVING
age()=31360, Philosopher 0 is thinking for 545 ms
age()=31910, Philosopher 0 wants to eat
age()=93150, Philosopher 2 is thinking for 462 ms
age()=93150, Philosopher 1 is eating for 59 ms
age()=93260, Philosopher 1 is thinking for 8682 ms
age()=93260, Philosopher 0 is eating for 22245 ms
age()=93650, Philosopher 2 wants to eat
age()=93650, Philosopher 2 is eating for 92309 ms
age()=101940, Philosopher 1 wants to eat
philosopher 1 is STARVING
age()=115510, Philosopher 0 is thinking for 614 ms
age()=116110, Philosopher 0 wants to eat
age()=120120, time to stop the Philosophers and exit
                                            ... end of example run(s)  */
