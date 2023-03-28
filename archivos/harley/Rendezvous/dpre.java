import java.util.Vector;
import Synchronization.*;
import Utilities.*;

class EatCondition extends MyObject implements RendezvousCondition {

   private int numPhils = 0;
   private int[] state = null;
   private int EATING = -1;

   public EatCondition(int[] state, int EATING) {
      super("EatCondition");
      this.state = state;
      numPhils = state.length;
      this.EATING = EATING;
   }

   private final int left(int i) { return (numPhils + i-1) % numPhils; }
   private final int right(int i) { return (i+1) % numPhils; }

   public boolean checkCondition(int messageNum, Vector blockedMessages,
         int numBlockedServers) {
      Object message = blockedMessages.elementAt(messageNum);
      int id = ((Integer) message).intValue();
      int size = blockedMessages.size();                // not used
      if (id < 0) return true;           // putForks()
      else if (state[left(id)] != EATING && state[right(id)] != EATING)
         return true;                    // takeForks()
      else return false;
   }
}

class DiningServer extends MyObject implements Runnable {

   private int numPhils = 0;
   private int[] state = null;
   private ConditionalRendezvous cr = null;
   private static final int THINKING = 0, HUNGRY = 1, EATING = 2;

   public DiningServer(int numPhils, boolean checkStarving) {
      super("DiningServer");
      this.numPhils = numPhils;
      state = new int[numPhils];
      for (int i = 0; i < numPhils; i++) state[i] = THINKING;
      cr = new ConditionalRendezvous(false);
      System.out.println("DiningServer: allows starvation");
      new Thread(this).start();
   }

   public void takeForks(int id) {
      state[id] = HUNGRY;                          // not used
      cr.clientTransactServer(new Integer(id));    // reply object discarded
   }

   public void putForks(int id) {
      cr.clientTransactServer(new Integer(-id-1)); // reply object discarded
   }

   public void run() {  // makes atomic state changes
      while (true) {
         Rendezvous r = cr.serverGetClient(new EatCondition(state, EATING));
         int id = ((Integer) r.serverGetRequest()).intValue();
         if (id < 0) state[-id-1] = THINKING;
         else state[id] = EATING;
         r.serverMakeReply(new Integer(0));
      }
   }
}

/* ............... Example compile and run(s)

% javac dpre.java dpdr.java

% java DiningPhilosophers -R15
DiningPhilosophers: numPhilosophers=5, checkStarving=false, runTime=15
DiningServer: allows starvation
Philosopher 0 is alive, napThink=8000, napEat=2000
age()=177, Philosopher 0 is thinking for 6203 ms
Philosopher 1 is alive, napThink=8000, napEat=2000
age()=232, Philosopher 1 is thinking for 3036 ms
Philosopher 2 is alive, napThink=8000, napEat=2000
age()=239, Philosopher 2 is thinking for 2223 ms
Philosopher 3 is alive, napThink=8000, napEat=2000
age()=246, Philosopher 3 is thinking for 5948 ms
Philosopher 4 is alive, napThink=8000, napEat=2000
age()=253, Philosopher 4 is thinking for 4108 ms
All Philosopher threads started
age()=2463, Philosopher 2 wants to eat
age()=2474, Philosopher 2 is eating for 1518 ms
age()=3293, Philosopher 1 wants to eat
age()=3994, Philosopher 2 is thinking for 6661 ms
age()=3999, Philosopher 1 is eating for 472 ms
age()=4423, Philosopher 4 wants to eat
age()=4427, Philosopher 4 is eating for 1574 ms
age()=4515, Philosopher 1 is thinking for 2332 ms
age()=6014, Philosopher 4 is thinking for 4058 ms
age()=6193, Philosopher 3 wants to eat
age()=6196, Philosopher 3 is eating for 51 ms
age()=6264, Philosopher 3 is thinking for 5489 ms
age()=6443, Philosopher 0 wants to eat
age()=6446, Philosopher 0 is eating for 845 ms
age()=6863, Philosopher 1 wants to eat
age()=7304, Philosopher 0 is thinking for 2479 ms
age()=7308, Philosopher 1 is eating for 959 ms
age()=8274, Philosopher 1 is thinking for 7588 ms
age()=9793, Philosopher 0 wants to eat
age()=9796, Philosopher 0 is eating for 555 ms
age()=10083, Philosopher 4 wants to eat
age()=10364, Philosopher 0 is thinking for 1612 ms
age()=10368, Philosopher 4 is eating for 137 ms
age()=10514, Philosopher 4 is thinking for 6758 ms
age()=10673, Philosopher 2 wants to eat
age()=10676, Philosopher 2 is eating for 1866 ms
age()=11763, Philosopher 3 wants to eat
age()=11993, Philosopher 0 wants to eat
age()=11996, Philosopher 0 is eating for 838 ms
age()=12554, Philosopher 2 is thinking for 1380 ms
age()=12558, Philosopher 3 is eating for 1739 ms
age()=12844, Philosopher 0 is thinking for 4332 ms
age()=13943, Philosopher 2 wants to eat
age()=14304, Philosopher 3 is thinking for 3218 ms
age()=14308, Philosopher 2 is eating for 1543 ms
age()=15313, time to stop the Philosophers and exit
                                            ... end of example run(s)  */
