package Synchronization;

// is final so subclassing cannot upset if (...) wait() and notify()
// but notify() (and wait() too) are public methods so still risky;
// better to create a private object and do notify(), wait() inside it
public final class CountingSemaphore extends Semaphore {

   public CountingSemaphore() {super();}  // constructors

   public CountingSemaphore(int initial) {super(initial);}
}
