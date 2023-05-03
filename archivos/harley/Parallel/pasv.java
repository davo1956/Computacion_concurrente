import java.util.*;
import java.io.*;

class Filter extends MyObject implements Runnable {

   private int id = -1;
   private int n = ParallelSieve.n;
   private MessagePassing[] sieve = ParallelSieve.sieve;
   private int[] seenCount = ParallelSieve.seenCount;
   private boolean debug = ParallelSieve.debug;

   public Filter(int id) { this.id = id; new Thread(this).start(); }

   public void run () {
      int prime = -1, number = -1;
      prime = receiveInt(sieve[id]);
      System.out.println("age()=" + age() + " filter " + id
         + " received prime " + prime);
      if (id == n-1) {
         System.out.println("done");
         System.out.println
            ("the filters each saw the following counts of numbers");
         for (int i = 0; i < n; i++) System.out.print(" " + seenCount[i]);
         System.out.println();
         System.exit(0);
      } else {
         while (true) {
            number = receiveInt(sieve[id]);
            seenCount[id]++;
            if (number % prime != 0) send(sieve[id+1], number);
            else if (debug)
               System.out.println("      *debug* filter " + id +
                  " discarding " + number + " (divisible by " + prime + ")");
         }
      }
   }
}

class ParallelSieve extends MyObject {

   public static int n = 8;
   public static MessagePassing[] sieve = null;
   public static int[] seenCount = null;
   public static boolean debug = false;

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Udn:");
      go.optErr = true;
      String usage = "Usage: -d -n number";
      int ch = -1;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'n')
            n = go.processArg(go.optArgGet(), n);
         else if ((char)ch == 'd') debug = true;
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      if (n < 1) {
         System.out.println("Generate at least one prime number.");
         System.exit(1);
      }
      System.out.println("ParallelSieve: debug=" + debug + ", n=" + n);
      System.out.println("age()=" + age() + ", generating the first "
         + n + " prime numbers greater than 2");
      sieve = new MessagePassing[n];
      sieve[0] = new SyncMessagePassing();  // blocking send
      for (int i = 1; i < n; i++) sieve[i] = new AsyncMessagePassing();
      seenCount = new int[n];
      for (int i = 0; i < n; i++) seenCount[i] = 1;
      for (int i = 0; i < n; i++) new Filter(i);
      int number = 3;
      while (true) {
         // blocking send so filter 0 gets only what it can use
         send(sieve[0], number);
         if (debug) System.out.println("      *debug* driver sent "
            + number + " to filter 0");
         number += 2;
      }
   }
}

/* ............... Example compile and run(s)

D:\>javac pasv.java

D:\>java ParallelSieve -d
ParallelSieve: debug=true, n=8
age()=50, generating the first 8 prime numbers greater than 2
      *debug* driver sent 3 to filter 0
age()=220 filter 0 received prime 3
      *debug* driver sent 5 to filter 0
age()=220 filter 1 received prime 5
      *debug* driver sent 7 to filter 0
      *debug* filter 0 discarding 9 (divisible by 3)
age()=270 filter 2 received prime 7
      *debug* driver sent 9 to filter 0
      *debug* driver sent 11 to filter 0
      *debug* driver sent 13 to filter 0
      *debug* filter 0 discarding 15 (divisible by 3)
age()=330 filter 3 received prime 11
      *debug* driver sent 15 to filter 0
age()=380 filter 4 received prime 13
      *debug* driver sent 17 to filter 0
      *debug* driver sent 19 to filter 0
age()=440 filter 5 received prime 17
      *debug* filter 0 discarding 21 (divisible by 3)
      *debug* driver sent 21 to filter 0
age()=490 filter 6 received prime 19
      *debug* driver sent 23 to filter 0
      *debug* driver sent 25 to filter 0
      *debug* filter 1 discarding 25 (divisible by 5)
      *debug* filter 0 discarding 27 (divisible by 3)
      *debug* driver sent 27 to filter 0
      *debug* driver sent 29 to filter 0
age()=550 filter 7 received prime 23
done
the filters each saw the following counts of numbers
 14 9 6 5 4 3 2 1

D:\>java ParallelSieve -n 100
ParallelSieve: debug=false, n=100
age()=0, generating the first 100 prime numbers greater than 2
age()=660 filter 0 received prime 3
age()=660 filter 1 received prime 5
age()=660 filter 2 received prime 7
   ...                                      // intervening output deleted
age()=2850 filter 97 received prime 523
age()=2910 filter 98 received prime 541
age()=2960 filter 99 received prime 547
done
the filters each saw the following counts of numbers
 320 213 169 144 131 120 113 108 106 105 103 102 101 100 99 98 97 96 95
  94 93 92 91 90 89 88 87 86 85 84 83 82 81 80 79 78 77 76 75 74 73 71
  70 67 66 64 63 62 61 60 59 58 57 56 55 54 51 50 49 48 47 46 44 43 42
  41 40 39 37 36 35 34 33 32 31 30 27 26 25 24 23 21 20 19 18 17 16 15
  14 13 12 11 10 9 8 7 6 5 4 1
                                            ... end of example run(s)  */
