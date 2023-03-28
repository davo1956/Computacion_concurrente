import Utilities.*;
import Synchronization.*;

class Pa extends ABCs implements Runnable {  // extend ABCs to
                                               // access semaphore sum
   public void run () {
      while (true) { nap(1+(int)(random(500)));
         System.out.print("A"); System.out.flush();
         V(sum);
      }
   }
}

class Pb extends ABCs implements Runnable {

   public void run () {
      while (true) { nap(1+(int)(random(800)));
         P(C); P(sum);
         System.out.print("B"); System.out.flush();
         V(B);
      }
   }
}

class Pc extends ABCs implements Runnable {

   public void run () {
      while (true) { nap(1+(int)(random(800)));
         P(B); P(sum);
         System.out.print("C"); System.out.flush();
         V(C);
      }
   }
}

class ABCs extends MyObject {

   protected static final BinarySemaphore B       // these semaphores
      = new BinarySemaphore(0);                   // are static
   protected static final BinarySemaphore C       // so subclasses
      = new BinarySemaphore(1);                   // Pa, Pb,
   protected static final CountingSemaphore sum   // and Pc share
      = new CountingSemaphore(0);                 // them

   public static void main(String[] args) {

      Thread pa = new Thread(new Pa());
      Thread pb = new Thread(new Pb());
      Thread pc = new Thread(new Pc());
      pa.start(); pb.start(); pc.start();
      nap(9000);
      pa.stop(); pb.stop(); pc.stop();
      System.out.println();
      System.out.println("B=" + B + ", C=" + C + ", sum=" + sum);
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac ABCs.java

D:\>java ABCs
ABACABACAAABCBAAAACAABCAABACBCAABCBAACABCABCABACABACABACABACABACABACABAACBAC
B=-1, C=1, sum=0

D:\>java ABCs
AAABCAABCAAABCAAABCABCAAAAAABACBCAABCBAACBCABCABACABAAACBAAAACBAACBAACBAA
B=1, C=-1, sum=7

D:\>java ABCs
AAABACABACABCBACABACABACABACABACABACABAAACBCABACABACABACAB
B=1, C=0, sum=0

D:\>java ABCs
ABACABACAAABAACBCABACBAACBAACBAAAACBACBAACBAACABACAABACAAABCBACAABCAAAABCAB
B=1, C=0, sum=5
                                            ... end of example run(s)  */
