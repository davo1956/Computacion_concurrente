import Utilities.*;
import Synchronization.*;

class CountingSemaphoreFromBinary extends MyObject {
   private int value = 0;
   private int waiting = 0;
   private BinarySemaphore mutex = new BinarySemaphore(1);
   private BinarySemaphore blocked = new BinarySemaphore(0);

   public CountingSemaphoreFromBinary(int n) {
      value = n;
   }

   public void down() {
      P(mutex);
      if (value == 0) {
         waiting++;
         V(mutex);
         // ...         // This is "point A" mentioned in the text.
         P(blocked);
      } else {
         value--;
         V(mutex);
      }
   }

   public void up() {
      P(mutex);
      if (value == 0 && waiting > 0) {
         waiting--;
         V(blocked);    // This is "point B" mentioned in the text.
      } else {
         value++;
      }
      V(mutex);
   }
}
