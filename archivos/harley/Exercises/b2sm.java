class AnotherBinarySemaphore {

   private boolean locked = false;

   public AnotherBinarySemaphore() {}  // constructors
   public AnotherBinarySemaphore(boolean initial) {locked = initial;}
   public AnotherBinarySemaphore(int initial) {
      if (initial < 0 || initial > 1)
         throw new IllegalArgumentException("initial<0 || initial>1");
      locked = (initial == 0);
   }

   public synchronized void P() {
      while (locked) {
         try { wait(); } catch (InterruptedException e) {}
      }
      locked = true;
   }

   public synchronized void V() {
      if (locked) notify();
      locked = false;
   }
}
