import Utilities.*;

class ATM extends Bank implements Runnable {
                 // inherits numAccounts, savingsAccount, mutex
   public void run() {
      int fromAccount, toAccount, amount;
      while (true) {
         fromAccount = (int) random(numAccounts);
         toAccount = (int) random(numAccounts);
         amount = 1 + (int) random(savingsAccount[fromAccount]);
         synchronized (mutex) {        // implied P(mutex)
            savingsAccount[fromAccount] -= amount;
            savingsAccount[toAccount] += amount;
         }                             // implied V(mutex)
      }
   }
}

class Auditor extends Bank implements Runnable {
                 // inherits numAccounts, savingsAccount, mutex
   public void run() {
      int total;
      while (true) {
         nap(1000);
         total = 0;
         synchronized (mutex) {        // implied P(mutex)
            for (int i = 0; i < numAccounts; i++)
               total += savingsAccount[i];
         }                             // implied V(mutex)
         System.out.println("age()=" + age() + ", total is $" + total);
      }
   }
}

class Bank extends MyObject {

   protected static final int numAccounts = 10000;
   private static final int initialValue = 1000; // dollars
   protected static final int[] savingsAccount
      = new int[numAccounts];
   protected static final Object mutex = new Object(); // semaphore

   public static void main(String[] args) {
 
      for (int i = 0; i < numAccounts; i++)
         savingsAccount[i] = initialValue;
      System.out.println("Bank open with " + numAccounts
         + " accounts, each starting with $" + initialValue);
      // enable time slicing Solaris (50 msec); noop on Windows 95
      ensureTimeSlicing(50); // so threads share CPU
      Thread atm = new Thread(new ATM());
      Thread auditor = new Thread(new Auditor());
      atm.start();
      auditor.start();
      nap(10000);
      atm.stop();
      System.out.println("age()=" + age() + ", ATM stopped");
      nap(3000);
      auditor.stop();
      System.out.println("age()=" + age() + ", Auditor stopped");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac nor2.java

D:\>java Bank
Bank open with 10000 accounts, each starting with $1000
age()=1150, total is $10000000
age()=2250, total is $10000000
age()=3290, total is $10000000
age()=4330, total is $10000000
age()=5380, total is $10000000
age()=6420, total is $10000000
age()=7470, total is $10000000
age()=8510, total is $10000000
age()=9550, total is $10000000
age()=10100, ATM stopped
age()=10540, total is $10000000
age()=11590, total is $10000000
age()=12570, total is $10000000
age()=13070, Auditor stopped
                                            ... end of example run(s)  */
