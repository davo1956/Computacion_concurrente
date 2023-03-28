// This is Java-like pseudocode, but NOT valid Java
// because the signaling discipline and condition
// variables are wrong.
import Utilities.*;
import Synchronization.*;

class DiningServer extends MyObject {

   private boolean checkStarving = false;
   private int numPhils = 0;
   private int[] state = null;
   private ConditionVariable[] self = null;
   private static final int
      THINKING = 0, HUNGRY = 1, STARVING = 2, EATING = 3;

   public DiningServer(int numPhils, boolean checkStarving) {
      this.numPhils = numPhils;
      this.checkStarving = checkStarving;
      state = new int[numPhils];
      for (int i = 0; i < numPhils; i++) state[i] = THINKING;
      self = new ConditionVariable[numPhils];
      for (int i = 0; i < numPhils; i++) self[i] = new ConditionVariable();
   }

   public void takeForks(int i) { hungryAndGetForks(i); }

   public void putForks(int i) {
      finishedEating(i);
      checkForkDown(left(i)); checkForkDown(right(i));
   }

   private final int left(int i) { return (numPhils + i - 1) % numPhils; }

   private final int right(int i) { return (i + 1) % numPhils; }

   private void seeIfStarving(int k) {
      if (state[k] == HUNGRY && state[left(k)] != STARVING &&
            state[right(k)] != STARVING) {
         state[k] = STARVING;
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

   private synchronized void hungryAndGetForks(int i) {
      state[i] = HUNGRY;
      test(i, false);
      if (state[i] != EATING) wait(self[i]);
   }

   private synchronized void finishedEating(int i) {
      state[i] = THINKING;
   }

   private synchronized void checkForkDown(int i) {
      test(i, checkStarving);
      if (state[i] == EATING) notify(self[i]);
   }
}
