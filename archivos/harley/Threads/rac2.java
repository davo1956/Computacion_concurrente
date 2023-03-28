import Utilities.*;

/*
 * Since
 *    volatile int[] savingsAccount = new int[numAccounts];
 * does not make each array component volatile, we have to use a class.
 */
class SavingsAccount { public volatile int balance = 0; }

class ATM extends Bank implements Runnable {
                        // inherits numAccounts, savingsAccount
   public void run() {
      int fromAccount, toAccount, amount;
      while (true) {
         fromAccount = (int) random(numAccounts);
         toAccount = (int) random(numAccounts);
         amount = 1 + (int) random(savingsAccount[fromAccount].balance);
         savingsAccount[fromAccount].balance -= amount;
         savingsAccount[toAccount].balance += amount;
      }
   }
}

class Auditor extends Bank implements Runnable {
                        // inherits numAccounts, savingsAccount
   public void run() {
      int total;
      while (true) {
         nap(1000);
         total = 0;
         for (int i = 0; i < numAccounts; i++)
            total += savingsAccount[i].balance;
         System.out.println("age()=" + age() + ", total is $" + total);
      }
   }
}

class Bank extends MyObject {

   protected static final int numAccounts = 10000;
   private static final int initialValue = 1000; // dollars
   protected static final SavingsAccount[] savingsAccount
/*
 * The above `final' makes the array reference savingsAccount a constant
 * but does not make each component of the array final, i.e., prevents
 * another assigment to savingsAccount but does not prevent another thread
 * from doing savingsAccount[i] = new SavingsAccount();
 */
      = new SavingsAccount[numAccounts];

   public static void main(String[] args) {
 
      for (int i = 0; i < numAccounts; i++) {
         savingsAccount[i] = new SavingsAccount();
         savingsAccount[i].balance = initialValue;
      }
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

D:\>javac rac2.java

D:\>java Bank
Bank open with 10000 accounts, each starting with $1000
age()=1100, total is $10000000
age()=2250, total is $10001948
age()=3300, total is $9994887
age()=4340, total is $9997471
age()=5380, total is $9988379
age()=6480, total is $9998827
age()=7530, total is $10000465
age()=8630, total is $10002009
age()=9780, total is $9997730
age()=10110, ATM stopped
age()=10820, total is $10000000
age()=11810, total is $10000000
age()=12850, total is $10000000
age()=13130, Auditor stopped
                                            ... end of example run(s)  */
