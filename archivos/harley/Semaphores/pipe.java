import Utilities.*;
import Synchronization.*;

class BoundedBuffer extends MyObject { // designed for a single
                      // producer thread and a single consumer thread
   private int numSlots = 0;
   private double[] buffer = null;
   private int putIn = 0, takeOut = 0;
   private CountingSemaphore elements = null;
   private CountingSemaphore spaces = null;

   public BoundedBuffer(int numSlots) {
      super("BoundedBuffer with " + numSlots + " slots");
      this.numSlots = numSlots;
      buffer = new double[numSlots];
      elements = new CountingSemaphore(0);
      spaces = new CountingSemaphore(numSlots);
   }

   public void deposit(double value) {
      P(spaces);
      buffer[putIn] = value;
      putIn = (putIn + 1) % numSlots;
      V(elements);
   }

   public double fetch() {
      double value;
      P(elements);
      value = buffer[takeOut];
      takeOut = (takeOut + 1) % numSlots;
      V(spaces);
      return value;
   }
}

class A extends MyObject implements Runnable {

   private int limit = 0;
   private BoundedBuffer ABbb = null;

   public A(int limit, BoundedBuffer ABbb) {
      this.limit = limit;
      this.ABbb = ABbb;
   }

   public void run() {
      double work;
      for (int i = 0; i < limit; i++) {
         work = random();
         System.out.println("A: work " + i + " ="  + work);
         work = Math.sqrt(work);      nap(1+(int)random(2000));
         ABbb.deposit(work);
      }
   }
}

class B extends MyObject implements Runnable {

   private int limit = 0;
   private BoundedBuffer ABbb = null;
   private BoundedBuffer BCbb = null;

   public B(int limit, BoundedBuffer ABbb, BoundedBuffer BCbb) {
      this.limit = limit;
      this.ABbb = ABbb;
      this.BCbb = BCbb;
   }

   public void run() {
      double work;
      for (int i = 0; i < limit; i++) {
         work = ABbb.fetch();
         work = Math.sin(work);      nap(1+(int)random(2000));
         System.out.println("      B: work " + i + " ="  + work);
         BCbb.deposit(work);
      }
   }
}

class C extends MyObject implements Runnable {

   private int limit = 0;
   private BoundedBuffer BCbb = null;

   public C(int limit, BoundedBuffer BCbb) {
      this.limit = limit;
      this.BCbb = BCbb;
   }

   public void run() {
      double work;
      for (int i = 0; i < limit; i++) {
         work = BCbb.fetch();
         work = work * work;      nap(1+(int)random(2000));
         System.out.println("            C: work " + i + " ="  + work);
      }
   }
}

class Piping extends MyObject {

   public static void main(String[] args) {

      // create the bounded buffers
      BoundedBuffer ABbb = new BoundedBuffer(4);
      BoundedBuffer BCbb = new BoundedBuffer(4);

      // start threads A, B, and C
      Thread a = new Thread(new A(9, ABbb));
      Thread b = new Thread(new B(9, ABbb, BCbb));
      Thread c = new Thread(new C(9, BCbb));
      a.start(); b.start(); c.start();
      System.out.println("All threads started");

      // wait for them to finish
      try {
         a.join(); b.join(); c.join();
      } catch (InterruptedException e) {}
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac pipe.java

D:\>java Piping
All threads started
A: work 0 =0.525258
A: work 1 =0.98132
      B: work 0 =0.662946
            C: work 0 =0.439497
A: work 2 =0.814105
A: work 3 =0.946678
      B: work 1 =0.836364
            C: work 1 =0.699504
      B: work 2 =0.784741
A: work 4 =0.12548
            C: work 2 =0.615818
A: work 5 =0.293954
      B: work 3 =0.826563
            C: work 3 =0.683207
      B: work 4 =0.346869
A: work 6 =0.1689
A: work 7 =0.46644
            C: work 4 =0.120318
A: work 8 =0.224181
      B: work 5 =0.516001
      B: work 6 =0.399503
            C: work 5 =0.266257
      B: work 7 =0.631095
      B: work 8 =0.455984
            C: work 6 =0.159603
            C: work 7 =0.398281
            C: work 8 =0.207921
                                            ... end of example run(s)  */
