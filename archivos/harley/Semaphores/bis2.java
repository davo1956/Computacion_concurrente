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
         // ...         // This is "point A" mentioned in the text.
         P(blocked);
      } else {
         V(mutex);
      }
   }

   public void up() {
      P(mutex);
      count++;
      if (count <= 0) {
         V(blocked);    // This is "point B" mentioned in the text.
      }
      V(mutex);
   }
}
