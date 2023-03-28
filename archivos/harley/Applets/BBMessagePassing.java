/*
 * Bounded buffer for objects with the message passing interface.
 * Designed for multiple producer threads and multiple consumer threads.
 */

public final class BBMessagePassing extends MessagePassingRoot {
   private int numSlots = 0;
   private Object[] buffer = null;
   private int putIn = 0, takeOut = 0;
   private CountingSemaphore elements = null;
   private CountingSemaphore spaces = null;
   private Object mutexS = null, mutexR = null;

   public BBMessagePassing(int numSlots) {
      super("BBMessagePassing, numSlots="+numSlots);
      if (numSlots <= 0) throw new IllegalArgumentException("numSlots<=0");
      this.numSlots = numSlots;
      buffer = new Object[numSlots];
      elements = new CountingSemaphore(0);
      spaces = new CountingSemaphore(numSlots);
      mutexS = new Object();
      mutexR = new Object();
   }

   public final void send(Object value) {
      if (value == null) {
         System.err.println("null message passed to send(), " + getName());
         throw new NullPointerException();
      }
/*
 * The P(spaces) is inside the synchronized (mutexS) block because otherwise
 * a context switch after the P(spaces) and before entering the synced block
 * might throw off FCFS ordering, a kind of race condition.
 */
      synchronized (mutexS) {
         P(spaces);
         buffer[putIn] = value;
         putIn = (putIn + 1) % numSlots;
         V(elements);
      }
   }

   public final Object receive() {
      Object value = null;
      synchronized (mutexR) {
/*
 * Ditto.
 */
         P(elements);
         value = buffer[takeOut];
         takeOut = (takeOut + 1) % numSlots;
         V(spaces);
      }
      return value;
   }

   public final String toString() { return getName(); }

// can add tryReceive() throws WouldBlockException by duplicating the
// receive() code above but changing the P(elements) into a elements.tryP()
}
