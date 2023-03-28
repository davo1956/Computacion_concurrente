import Utilities.*;
import Synchronization.*;

class Problem { public int x, y;
   public Problem(int x, int y) { this.x = x;  this.y = y; }
}

class GenerateProblem extends MyObject implements Runnable {

   private MessagePassing gs = null, ca = null;

   public GenerateProblem(MessagePassing gs, MessagePassing ca) {
      this.gs = gs;  this.ca = ca;
   }

   public void run () {
      System.out.println("age()=" + age() + ", GP: generating a problem");
      nap(1000);
      System.out.println("age()=" + age() + ", GP: sending the problem");
      send(gs, new Problem(1, 2));
      System.out.println("age()=" + age() + ", GP: the problem is sent");
      nap(4000);
      System.out.println("age()=" + age() + ", GP: waiting for the answer");
      int z = ((Integer) receive(ca)).intValue();
      System.out.println("age()=" + age() + ", GP: the answer is " + z);
   }
}

class ComputeAnswer extends MyObject implements Runnable {

   private MessagePassing gs = null, ca = null;

   public ComputeAnswer(MessagePassing gs, MessagePassing ca) {
      this.gs = gs;  this.ca = ca;
   }

   public void run () {
      nap(2000);
      System.out.println("age()=" + age() + ", CA: waiting for a problem");
      Problem p = (Problem) receive(gs);
      System.out.println("age()=" + age() + ", CA: the problem is received");
      nap(1000);
      System.out.println("age()=" + age() + ", CA: sending the answer");
      send(ca, new Integer(p.x + p.y));
      System.out.println("age()=" + age() + ", CA: the answer is delivered");
   }
}

class SendReceive extends MyObject {

   public static void main(String[] args) {
      // non-blocking send()
      AsyncMessagePassing gs = new AsyncMessagePassing();
      MessagePassing gsS = new MessagePassingSendOnly(gs);
      MessagePassing gsR = new MessagePassingReceiveOnly(gs);
      // blocking send()
      SyncMessagePassing ca = new SyncMessagePassing();
      MessagePassing caS = new MessagePassingSendOnly(ca);
      MessagePassing caR = new MessagePassingReceiveOnly(ca);
      Thread gsThread = new Thread(new GenerateProblem(gsS, caR));
      Thread caThread = new Thread(new ComputeAnswer(gsR, caS));
      gsThread.start();  caThread.start();
      try {
         gsThread.join();  caThread.join();
      } catch (InterruptedException e) {
         System.out.println("SendReceive interrupted");
         System.exit(1);
      }
      System.out.println("SendReceive done");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac snrc.java

D:\>java SendReceive
age()=0, GP: generating a problem
age()=1050, GP: sending the problem
age()=1050, GP: the problem is sent
age()=2040, CA: waiting for a problem
age()=2040, CA: the problem is received
age()=3080, CA: sending the answer
age()=5060, GP: waiting for the answer
age()=5060, GP: the answer is 3
age()=5060, CA: the answer is delivered
SendReceive done
                                            ... end of example run(s)  */
