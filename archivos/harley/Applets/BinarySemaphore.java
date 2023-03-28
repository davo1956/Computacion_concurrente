// is final so subclassing cannot upset if (...) wait() and notify()
// but notify() (and wait() too) are public methods so still risky;
// better to create a private object and do notify(), wait() inside it
public final class BinarySemaphore extends Semaphore {

   public BinarySemaphore() {super();}  // constructors

//   public BinarySemaphore(int initial) {super((initial!=0) ? 1:0);}
   public BinarySemaphore(int initial) {
      super(initial);
      // Don't be silent about bad initial value; tell the user!
      if (initial > 1) throw new IllegalArgumentException("initial>1");
   }

   public BinarySemaphore(boolean initial) {super(initial ? 1:0);}

   public final synchronized void V() {
      super.V();
      if (value > 1) value = 1; // cap the value
   }
}
