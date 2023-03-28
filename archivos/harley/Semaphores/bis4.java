import Utilities.*;
import Synchronization.*;

class CountingSemaphoreFromBinary extends MyObject {
   private int count = 0;
   private BinarySemaphore mutex = new BinarySemaphore(1);
   private BinarySemaphore blocked = new BinarySemaphore(0);
   private int wakeup = 0;  // new variable

   public CountingSemaphoreFromBinary(int n) {
      count = n;
   }

   public void down() {
      P(mutex);
      count--;
      if (count < 0) {
         V(mutex);
         P(blocked);
         P(mutex);
         wakeup--;
         if (wakeup > 0) V(blocked);
      }
      V(mutex);
   }

   public void up() {
      P(mutex);
      count++;
      if (count <= 0) {
         wakeup++;
         V(blocked);
      }
      V(mutex);
   }
}
