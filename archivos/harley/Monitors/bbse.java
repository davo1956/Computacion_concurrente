// This is Java-like pseudocode, but NOT valid Java
// because the signaling discipline and condition
// variables are wrong.
import Utilities.*;
import Synchronization.*;

class BoundedBuffer extends MyObject {

   private int size = 0;
   private double[] buf = null;
   private int front = 0, rear = 0, count = 0;
   private ConditionVariable notFull = null;
   private ConditionVariable notEmpty = null;

   public BoundedBuffer(int size) {
      this.size = size;
      buf = new double[size];
      notFull = new ConditionVariable();
      notEmpty = new ConditionVariable();
   }

   public synchronized void deposit(double data) {
      if (count == size) wait(notFull);
      buf[rear] = data;
      rear = (rear+1) % size;
      count++;
      if (count == 1) notify(notEmpty);
   }

   public synchronized double fetch() {
      double result;
      if (count == 0) wait(notEmpty);
      result = buf[front];
      front = (front+1) % size;
      count--;
      if (count == size-1) notify(notFull);
      return result;
   }
}
