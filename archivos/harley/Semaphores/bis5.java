import Utilities.*;
import Synchronization.*;

class CountingSemaphoreFromBinary extends MyObject {
   private int count = 0;
   private BinarySemaphore mutex = new BinarySemaphore(1);
   private BinarySemaphore blocked = new BinarySemaphore(0);
   private BinarySemaphore serial = new BinarySemaphore(1);

   public CountingSemaphoreFromBinary(int n) {
      count = n;
   }

   public void down() {
      P(serial);
      P(mutex);
      count--;
      if (count < 0) {
         V(mutex);
         P(blocked);
      } else {
         V(mutex);
      }
      V(serial);
   }

   public void up() {
      P(mutex);
      count++;
      if (count <= 0) {
         V(blocked);
      }
      V(mutex);
   }
}
