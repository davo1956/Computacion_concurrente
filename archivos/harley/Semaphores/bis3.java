import Utilities.*;
import Synchronization.*;

class CountingSemaphoreFromBinary extends MyObject {
   private int count = 0;
   private BinarySemaphore mutex = new BinarySemaphore(1);
   private BinarySemaphore blocked = new BinarySemaphore(0);

   public CountingSemaphoreFromBinary(int n) {
      count = n;
   }

   public void down() {
      P(mutex);
      count--;
      if (count < 0) {
         V(mutex);
         P(blocked);
      }
      V(mutex);
   }

   public void up() {
      P(mutex);
      count++;
      if (count <= 0) {
         V(blocked);
      } else {
         V(mutex);
      }
   }
}
