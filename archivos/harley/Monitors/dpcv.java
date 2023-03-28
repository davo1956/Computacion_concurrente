import Utilities.*;

class DiningServer extends MyObject {

   private boolean checkStarving = false;
   private int numPhils = 0;
   private int[] state = null;
   private Object[] convey = null;
   private static final int
      THINKING = 0, HUNGRY = 1, STARVING = 2, EATING = 3;

   public DiningServer(int numPhils, boolean checkStarving) {
      super("DiningServer");
      this.numPhils = numPhils;
      this.checkStarving = checkStarving;
      state = new int[numPhils];
      for (int i = 0; i < numPhils; i++) state[i] = THINKING;
      convey = new Object[numPhils];
      for (int i = 0; i < numPhils; i++) convey[i] = new Object();
      System.out.println("DiningServer: checkStarving="
         + checkStarving);
   }

   private final int left(int i) { return (numPhils + i-1) % numPhils; }
   private final int right(int i) { return (i+1) % numPhils; }

   public void takeForks(int i) {
      synchronized (convey[i]) {
         if (hungryAndGetForks(i)) return;
         else while (true)   // we must be notified not interrupted
            try { convey[i].wait();  break; }
            // notify() after interrupt() race condition ignored
            catch (InterruptedException e) { continue; }
      }
   }

   public synchronized void putForks(int i) {
      state[i] = THINKING;
      test(left(i), checkStarving);
      test(right(i), checkStarving);
      if (state[left(i)] == EATING) forksAvailable(left(i));
      if (state[right(i)] == EATING) forksAvailable(right(i));
   }

   private synchronized boolean hungryAndGetForks(int i) {
      state[i] = HUNGRY;
      test(i, false);
      return state[i] == EATING;
   }

   private void forksAvailable(int i) {
      synchronized (convey[i]) {
         convey[i].notify();
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

   private void seeIfStarving(int k) {
      if (state[k] == HUNGRY && state[left(k)] != STARVING &&
            state[right(k)] != STARVING) {
         state[k] = STARVING;
         System.out.println("age()=" + age() + ", philosopher " + k
            + " is STARVING!");
      }
   }
}

/* ............... Example compile and run(s)

D:\>javac dpcv.java dpdr.java

D:\>java DiningPhilosophers -S -R15
DiningPhilosophers: numPhilosophers=5, checkStarving=true, runTime=15
DiningServer: checkStarving=true
Philosopher 0 is alive, napThink=8000, napEat=2000
Philosopher 1 is alive, napThink=8000, napEat=2000
Philosopher 2 is alive, napThink=8000, napEat=2000
Philosopher 3 is alive, napThink=8000, napEat=2000
Philosopher 4 is alive, napThink=8000, napEat=2000
All Philosopher threads started
age()=60, Philosopher 0 is thinking for 2774 ms
age()=60, Philosopher 1 is thinking for 1038 ms
age()=60, Philosopher 2 is thinking for 6942 ms
age()=60, Philosopher 3 is thinking for 6202 ms
age()=60, Philosopher 4 is thinking for 4087 ms
age()=1100, Philosopher 1 wants to eat
age()=1100, Philosopher 1 is eating for 1490 ms
age()=2580, Philosopher 1 is thinking for 564 ms
age()=2800, Philosopher 0 wants to eat
age()=2800, Philosopher 0 is eating for 1384 ms
age()=3190, Philosopher 1 wants to eat
age()=4120, Philosopher 4 wants to eat
age()=4230, Philosopher 0 is thinking for 6211 ms
age()=4230, Philosopher 4 is eating for 1373 ms
age()=4230, Philosopher 1 is eating for 1703 ms
age()=5600, Philosopher 4 is thinking for 5579 ms
age()=5930, Philosopher 1 is thinking for 2500 ms
age()=6260, Philosopher 3 wants to eat
age()=6260, Philosopher 3 is eating for 691 ms
age()=6980, Philosopher 3 is thinking for 4170 ms
age()=6980, Philosopher 2 wants to eat
age()=6980, Philosopher 2 is eating for 1492 ms
age()=8460, Philosopher 1 wants to eat
age()=8520, Philosopher 2 is thinking for 7654 ms
age()=8520, Philosopher 1 is eating for 1128 ms
age()=9610, Philosopher 1 is thinking for 5315 ms
age()=10440, Philosopher 0 wants to eat
age()=10440, Philosopher 0 is eating for 920 ms
age()=11150, Philosopher 3 wants to eat
age()=11150, Philosopher 3 is eating for 756 ms
age()=11210, Philosopher 4 wants to eat
age()=11370, philosopher 4 is STARVING!
age()=11370, Philosopher 0 is thinking for 66 ms
age()=11430, Philosopher 0 wants to eat
age()=11870, Philosopher 3 is thinking for 80 ms
age()=11870, Philosopher 4 is eating for 938 ms
age()=11980, Philosopher 3 wants to eat
age()=12850, Philosopher 4 is thinking for 5500 ms
age()=12850, Philosopher 3 is eating for 1628 ms
age()=12850, Philosopher 0 is eating for 1684 ms
age()=14450, Philosopher 3 is thinking for 4027 ms
age()=14560, Philosopher 0 is thinking for 5920 ms
age()=14940, Philosopher 1 wants to eat
age()=14940, Philosopher 1 is eating for 578 ms
age()=15050, time to stop the Philosophers and exit
                                            ... end of example run(s)  */
