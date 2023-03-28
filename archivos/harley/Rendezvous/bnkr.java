import java.util.Vector;
import Synchronization.*;
import Utilities.*;

class WithdrawCondition extends MyObject implements RendezvousCondition {

   private int balance = 0;
   private boolean allDepositsBeforeAnyWithdrawals = false;

   public WithdrawCondition(int balance,
         boolean allDepositsBeforeAnyWithdrawals) {
      super("WithdrawCondition: balance=" + balance
         + " allDepositsBeforeAnyWithdrawals="
         + allDepositsBeforeAnyWithdrawals);
      this.balance = balance;
      this.allDepositsBeforeAnyWithdrawals = allDepositsBeforeAnyWithdrawals;
   }

   public boolean checkCondition(int messageNum, Vector blockedMessages,
         int numBlockedServers) {
      Object message = blockedMessages.elementAt(messageNum);
      int amount = ((Integer) message).intValue();
      int size = blockedMessages.size();  // count number waiting deposits

      if (allDepositsBeforeAnyWithdrawals) {
         if (amount > 0) return true; // deposit is okay
         int numBlockedDeposits = 0; // count number waiting deposits
         for (int i = 0; i < size; i++) {
            Object m = blockedMessages.elementAt(i);
            int a = ((Integer) m).intValue();
            if (a > 0) numBlockedDeposits++;
         }
         if (numBlockedDeposits > 0 /* && amount < 0 */) return false;
         else return -amount < balance;
      } else {

            // special case (-1): any deposit or withdrawal is okay
         if (balance < 0) return true;

            // any deposit is okay but withdrawals must pass next test
         else if ( /* balance >= 0 && */ amount > 0) return true;

            // if we get here then balance >= 0 and amount <= 0,
            // so a |withdrawal| < balance is okay;
            // but if balance==0 (special flag value) then
            // no withdrawals are allowed
         else return -amount < balance;
      }
   }
}

class Depositor extends MyObject implements Runnable {

   private int dNap = 0; // milliseconds
   private ConditionalRendezvous cr = null;

   public Depositor(String name, int dNap, ConditionalRendezvous cr) {
      super(name);
      this.dNap = dNap;
      this.cr = cr;
      new Thread(this).start();
   }

   public void run() {
      int deposit, napping, put;
      while (true) {
         napping = 1 + (int) random(dNap);
         System.out.println("age=" + age() + ", " + getName()
            + " napping for " + napping + " ms");
         nap(napping);
         deposit = 1 + (int) random(dNap);
         System.out.println("age=" + age() + ", " + getName()
            + " wants to deposit " + deposit);
         put = ((Integer) cr.clientTransactServer(
               new Integer(deposit))).intValue();
         System.out.println("age=" + age() + ", " + getName()
            + " deposited " + put);
      }
   }
}

class Withdrawer extends MyObject implements Runnable {

   private int wNap = 0; // milliseconds
   private ConditionalRendezvous cr = null;

   public Withdrawer(String name, int wNap, ConditionalRendezvous cr) {
      super(name);
      this.wNap = wNap;
      this.cr = cr;
      new Thread(this).start();
   }

   public void run() {
      int withdraw, napping, got;
      while (true) {
         napping = 1 + (int) random(wNap);
         System.out.println("age=" + age() + ", " + getName()
            + " napping for " + napping + " ms");
         nap(napping);
         withdraw = 1 + (int) random(wNap);
         System.out.println("age=" + age() + ", " + getName()
            + " wants to withdraw " + withdraw);
         got = ((Integer) cr.clientTransactServer(
               new Integer(-withdraw))).intValue();
         System.out.println("age=" + age() + ", " + getName()
            + " was able to withdraw " + got);
      }
   }
}

class Bank extends MyObject implements Runnable {

   private ConditionalRendezvous cr = null;
   private boolean starvationFree = false;
   private boolean allDepositsBeforeAnyWithdrawals = false;
   private int initialBalance = 0;

   public Bank(String name, int initialBalance, ConditionalRendezvous cr,
         boolean starvationFree, boolean allDepositsBeforeAnyWithdrawals) {
      super(name);
      this.initialBalance = initialBalance;
      this.cr = cr;
      this.starvationFree = starvationFree;
      this.allDepositsBeforeAnyWithdrawals = allDepositsBeforeAnyWithdrawals;
      new Thread(this).start();
   }

   public void run() {
      int balance = initialBalance;
      System.out.println("age=" + age() + ", " + getName()
         + " is open with initial balance of " + balance
         + ",\n starvation free is " + starvationFree
         + ", allDepositsBeforeAnyWithdrawals="
         + allDepositsBeforeAnyWithdrawals);
      while (true) {
         if (starvationFree) {
            // take anything (-1 is special flag value for this)
            RendezvousCondition c = new WithdrawCondition(-1, false);
            Rendezvous r = cr.serverGetClient(c);
            int amount = ((Integer) r.serverGetRequest()).intValue();
            if (amount > 0) {                // deposit
               System.out.println("age=" + age() + ", " + getName()
                  + " processing deposit of " + amount
                  + "\n          against balance of " + balance);
               balance += amount;
               nap(1 + (int) random(1000));
               System.out.println("age=" + age() + ", " + getName()
                  + " says NEW balance is " + balance);
               r.serverMakeReply(new Integer(amount));
            } else if (-amount < balance) { // allowed withdrawal
               System.out.println("age=" + age() + ", " + getName()
                  + " processing allowed withdrawal of " + amount
                  + "\n          against balance of " + balance);
               balance += amount;
               nap(1 + (int) random(1000));
               System.out.println("age=" + age() + ", " + getName()
                  + " says NEW balance is " + balance);
               r.serverMakeReply(new Integer(-amount));
            } else {       // withdrawal too big; take deposits until okay
               System.out.println("age=" + age() + ", " + getName()
                  + " deferring withdrawal of " + (-amount)
                  + ",\n take deposits only to build balance up");
               while (-amount >= balance) {
                  // deposits only (0 is special flag value for this)
                  RendezvousCondition nc = new WithdrawCondition(0, false);
                  Rendezvous nr = cr.serverGetClient(nc);
                  int deposit = ((Integer) nr.serverGetRequest()).intValue();
                  System.out.println("age=" + age() + ", " + getName()
                     + " processing nested deposit of " + deposit
                     + "\n          against balance of " + balance);
                  balance += deposit;
                  nap(1 + (int) random(1000));
                  System.out.println("age=" + age() + ", " + getName()
                     + " says NEW balance is " + balance);
                  nr.serverMakeReply(new Integer(deposit));
               }
               System.out.println("age=" + age() + ", " + getName()
                  + " processing deferred withdrawal of " + (-amount)
                  + "\n          against balance of " + balance);
               balance += amount;
               nap(1 + (int) random(1000));
               System.out.println("age=" + age() + ", " + getName()
                  + " says NEW balance is " + balance);
               r.serverMakeReply(new Integer(-amount));
            }
         } else {  // take only deposits and allowed withdrawals
                   // but a large withdrawal may "starve" in the queue
            RendezvousCondition c = new WithdrawCondition(balance,
               allDepositsBeforeAnyWithdrawals);
            Rendezvous r = cr.serverGetClient(c);
            int amount = ((Integer) r.serverGetRequest()).intValue();
            System.out.println("age=" + age() + ", " + getName()
               + " processing transaction for " + amount
               + "\n          against balance of " + balance);
            balance += amount;
            nap(1 + (int) random(1000));
            System.out.println("age=" + age() + ", " + getName()
               + " says NEW balance is " + balance);
            r.serverMakeReply(new Integer(Math.abs(amount)));
         }
      }
   }
}

class DepositWithdraw extends MyObject {

   public static void main(String[] args) {

      // parse command line arguments, if any, to override defaults
      GetOpt go = new GetOpt(args, "Uabsi:B:D:W:d:w:R:");
      go.optErr = true;
      String usage = "Usage: -a -b -s -i initBal -B numB"
           + " -D numD -W numW -d dNap -w wNap -R runTime";
      int ch = -1;
      boolean allDepositsBeforeAnyWithdrawals = false;
      boolean debug = false;
      boolean starvationFree = false;
      int initialBalance = 1000;
      int numBanks = 1;
      int numDepositors = 2;
      int numWithdrawers = 3;
      int dNap = 2;       // defaults
      int wNap = 3;       // in
      int runTime = 60;   // seconds
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'a') allDepositsBeforeAnyWithdrawals = true;
         else if ((char)ch == 'b') debug = true;
         else if ((char)ch == 's') starvationFree = true;
         else if ((char)ch == 'i')
            initialBalance = go.processArg(go.optArgGet(), initialBalance);
         else if ((char)ch == 'B')
            numBanks = go.processArg(go.optArgGet(), numBanks);
         else if ((char)ch == 'D')
            numDepositors = go.processArg(go.optArgGet(), numDepositors);
         else if ((char)ch == 'W')
            numWithdrawers = go.processArg(go.optArgGet(), numWithdrawers);
         else if ((char)ch == 'd')
            dNap = go.processArg(go.optArgGet(), dNap);
         else if ((char)ch == 'w')
            wNap = go.processArg(go.optArgGet(), wNap);
         else if ((char)ch == 'R')
            runTime = go.processArg(go.optArgGet(), runTime);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("DepositWithdraw: numBanks=" + numBanks
         + "\n starvationFree=" + starvationFree + ", debug=" + debug
         + ", initialBalance=" + initialBalance + "\n numDepositors="
         + numDepositors + ", numWithdrawers=" + numWithdrawers
         + ", dNap=" + dNap + ", wNap=" + wNap + ", runTime=" + runTime);
      if (allDepositsBeforeAnyWithdrawals) System.out.println
         ("All waiting deposits will be done before any withdrawals.");

      // create the conditional rendezvous
         ConditionalRendezvous cr = new ConditionalRendezvous(debug);

      // start the bank threads
      for (int i = 0; i < numBanks; i++)
         new Bank("Bank"+i, initialBalance, cr,
            starvationFree, allDepositsBeforeAnyWithdrawals);

      // start the Depositor and Withdrawer threads
      for (int i = 0; i < numDepositors; i++)
         new Depositor("Depositor"+i, dNap*1000, cr);
      for (int i = 0; i < numWithdrawers; i++)
         new Withdrawer("Withdrawer"+i, wNap*1000, cr);
      System.out.println("All threads started");

      // let them run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the threads and exit");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

% javac bnkr.java

% java DepositWithdraw -R10
DepositWithdraw: numBanks=1
 starvationFree=false, debug=false, initialBalance=1000
 numDepositors=2, numWithdrawers=3, dNap=2, wNap=3, runTime=10
age=159, Bank0 is open with initial balance of 1000,
 starvation free is false, allDepositsBeforeAnyWithdrawals=false
age=214, Depositor0 napping for 918 ms
age=263, Depositor1 napping for 1028 ms
age=328, Withdrawer0 napping for 930 ms
age=331, Withdrawer1 napping for 191 ms
age=335, Withdrawer2 napping for 2905 ms
All threads started
age=541, Withdrawer1 wants to withdraw 392
age=552, Bank0 processing transaction for -392
          against balance of 1000
age=1001, Bank0 says NEW balance is 608
age=1010, Withdrawer1 was able to withdraw 392
age=1014, Withdrawer1 napping for 1255 ms
age=1241, Depositor0 wants to deposit 1954
age=1246, Bank0 processing transaction for 1954
          against balance of 608
age=1271, Withdrawer0 wants to withdraw 2345
age=1375, Depositor1 wants to deposit 1386
age=2151, Bank0 says NEW balance is 2562
age=2154, Depositor0 deposited 1954
age=2156, Depositor0 napping for 1531 ms
age=2160, Bank0 processing transaction for -2345
          against balance of 2562
age=2291, Withdrawer1 wants to withdraw 585
age=2971, Bank0 says NEW balance is 217
age=2974, Withdrawer0 was able to withdraw 2345
age=2976, Withdrawer0 napping for 2092 ms
age=2980, Bank0 processing transaction for 1386
          against balance of 217
age=3251, Withdrawer2 wants to withdraw 2629
age=3296, Bank0 says NEW balance is 1603
age=3299, Depositor1 deposited 1386
age=3301, Depositor1 napping for 1417 ms
age=3305, Bank0 processing transaction for -585
          against balance of 1603
age=3631, Bank0 says NEW balance is 1018
age=3634, Withdrawer1 was able to withdraw 585
age=3636, Withdrawer1 napping for 2378 ms
age=3701, Depositor0 wants to deposit 663
age=3705, Bank0 processing transaction for 663
          against balance of 1018
age=3881, Bank0 says NEW balance is 1681
age=3884, Depositor0 deposited 663
age=3886, Depositor0 napping for 1110 ms
age=4731, Depositor1 wants to deposit 525
age=4735, Bank0 processing transaction for 525
          against balance of 1681
age=5001, Depositor0 wants to deposit 1589
age=5081, Withdrawer0 wants to withdraw 362
age=5671, Bank0 says NEW balance is 2206
age=5674, Depositor1 deposited 525
age=5676, Depositor1 napping for 796 ms
age=5680, Bank0 processing transaction for 1589
          against balance of 2206
age=6031, Withdrawer1 wants to withdraw 1827
age=6331, Bank0 says NEW balance is 3795
age=6334, Depositor0 deposited 1589
age=6336, Depositor0 napping for 324 ms
age=6340, Bank0 processing transaction for -2629
          against balance of 3795
age=6491, Depositor1 wants to deposit 125
age=6671, Depositor0 wants to deposit 1907
age=7101, Bank0 says NEW balance is 1166
age=7104, Withdrawer2 was able to withdraw 2629
age=7106, Withdrawer2 napping for 2457 ms
age=7110, Bank0 processing transaction for -362
          against balance of 1166
age=7901, Bank0 says NEW balance is 804
age=7904, Withdrawer0 was able to withdraw 362
age=7906, Withdrawer0 napping for 652 ms
age=7910, Bank0 processing transaction for 125
          against balance of 804
age=8521, Bank0 says NEW balance is 929
age=8524, Depositor1 deposited 125
age=8526, Depositor1 napping for 990 ms
age=8529, Bank0 processing transaction for 1907
          against balance of 929
age=8581, Withdrawer0 wants to withdraw 2532
age=8584, Bank0 says NEW balance is 2836
age=8588, Depositor0 deposited 1907
age=8591, Depositor0 napping for 476 ms
age=8593, Bank0 processing transaction for -1827
          against balance of 2836
age=9081, Depositor0 wants to deposit 1147
age=9126, Bank0 says NEW balance is 1009
age=9129, Withdrawer1 was able to withdraw 1827
age=9131, Withdrawer1 napping for 2058 ms
age=9135, Bank0 processing transaction for 1147
          against balance of 1009
age=9531, Depositor1 wants to deposit 1035
age=9571, Withdrawer2 wants to withdraw 202
age=9711, Bank0 says NEW balance is 2156
age=9714, Depositor0 deposited 1147
age=9716, Depositor0 napping for 379 ms
age=9720, Bank0 processing transaction for 1035
          against balance of 2156
age=10031, Bank0 says NEW balance is 3191
age=10034, Depositor1 deposited 1035
age=10036, Depositor1 napping for 359 ms
age=10040, Bank0 processing transaction for -2532
          against balance of 3191
age=10111, Depositor0 wants to deposit 913
age=10301, Bank0 says NEW balance is 659
age=10304, Withdrawer0 was able to withdraw 2532
age=10306, Withdrawer0 napping for 560 ms
age=10310, Bank0 processing transaction for -202
          against balance of 659
age()=10342, time to stop the threads and exit

% java DepositWithdraw -a -D5 -W2 -R10
DepositWithdraw: numBanks=1
 starvationFree=false, debug=false, initialBalance=1000
 numDepositors=5, numWithdrawers=2, dNap=2, wNap=3, runTime=10
All waiting deposits will be done before any withdrawals.
age=206, Bank0 is open with initial balance of 1000,
 starvation free is false, allDepositsBeforeAnyWithdrawals=true
age=261, Depositor0 napping for 1225 ms
age=308, Depositor1 napping for 862 ms
age=313, Depositor2 napping for 1380 ms
age=320, Depositor3 napping for 1692 ms
age=330, Depositor4 napping for 497 ms
All threads started
age=397, Withdrawer1 napping for 2503 ms
age=393, Withdrawer0 napping for 743 ms
age=914, Depositor4 wants to deposit 1844
age=924, Bank0 processing transaction for 1844
          against balance of 1000
age=1174, Withdrawer0 wants to withdraw 477
age=1264, Depositor1 wants to deposit 1791
age=1544, Depositor0 wants to deposit 1826
age=1754, Bank0 says NEW balance is 2844
age=1762, Depositor4 deposited 1844
age=1764, Depositor4 napping for 228 ms
age=1768, Bank0 processing transaction for 1791
          against balance of 2844
age=1811, Depositor2 wants to deposit 1938
age=1974, Bank0 says NEW balance is 4635
age=1977, Depositor1 deposited 1791
age=1979, Depositor1 napping for 291 ms
age=1982, Bank0 processing transaction for 1826
          against balance of 4635
age=2025, Depositor4 wants to deposit 511
age=2115, Depositor3 wants to deposit 532
age=2284, Depositor1 wants to deposit 1764
age=2374, Bank0 says NEW balance is 6461
age=2376, Depositor0 deposited 1826
age=2379, Depositor0 napping for 1472 ms
age=2383, Bank0 processing transaction for 1938
          against balance of 6461
age=2425, Bank0 says NEW balance is 8399
age=2428, Depositor2 deposited 1938
age=2430, Depositor2 napping for 1919 ms
age=2434, Bank0 processing transaction for 511
          against balance of 8399
age=2694, Bank0 says NEW balance is 8910
age=2696, Depositor4 deposited 511
age=2698, Depositor4 napping for 1618 ms
age=2702, Bank0 processing transaction for 532
          against balance of 8910
age=2934, Withdrawer1 wants to withdraw 2523
age=3044, Bank0 says NEW balance is 9442
age=3046, Depositor3 deposited 532
age=3049, Depositor3 napping for 881 ms
age=3052, Bank0 processing transaction for 1764
          against balance of 9442
age=3224, Bank0 says NEW balance is 11206
age=3227, Depositor1 deposited 1764
age=3230, Depositor1 napping for 730 ms
age=3234, Bank0 processing transaction for -477
          against balance of 11206
age=3362, Bank0 says NEW balance is 10729
age=3364, Withdrawer0 was able to withdraw 477
age=3367, Withdrawer0 napping for 1135 ms
age=3371, Bank0 processing transaction for -2523
          against balance of 10729
age=3874, Depositor0 wants to deposit 1816
age=3944, Depositor3 wants to deposit 797
age=3989, Depositor1 wants to deposit 1673
age=4234, Bank0 says NEW balance is 8206
age=4237, Withdrawer1 was able to withdraw 2523
age=4239, Withdrawer1 napping for 2085 ms
age=4243, Bank0 processing transaction for 1816
          against balance of 8206
age=4334, Depositor4 wants to deposit 782
age=4379, Depositor2 wants to deposit 1548
age=4434, Bank0 says NEW balance is 10022
age=4437, Depositor0 deposited 1816
age=4439, Depositor0 napping for 570 ms
age=4443, Bank0 processing transaction for 797
          against balance of 10022
age=4494, Bank0 says NEW balance is 10819
age=4497, Depositor3 deposited 797
age=4499, Depositor3 napping for 525 ms
age=4503, Bank0 processing transaction for 1673
          against balance of 10819
age=4545, Withdrawer0 wants to withdraw 1040
age=5014, Depositor0 wants to deposit 419
age=5059, Depositor3 wants to deposit 1697
age=5294, Bank0 says NEW balance is 12492
age=5297, Depositor1 deposited 1673
age=5299, Depositor1 napping for 797 ms
age=5303, Bank0 processing transaction for 782
          against balance of 12492
age=6114, Depositor1 wants to deposit 442
age=6284, Bank0 says NEW balance is 13274
age=6287, Depositor4 deposited 782
age=6289, Depositor4 napping for 326 ms
age=6293, Bank0 processing transaction for 1548
          against balance of 13274
age=6344, Withdrawer1 wants to withdraw 676
age=6624, Depositor4 wants to deposit 392
age=6864, Bank0 says NEW balance is 14822
age=6867, Depositor2 deposited 1548
age=6869, Depositor2 napping for 1448 ms
age=6873, Bank0 processing transaction for 419
          against balance of 14822
age=7804, Bank0 says NEW balance is 15241
age=7807, Depositor0 deposited 419
age=7809, Depositor0 napping for 536 ms
age=7813, Bank0 processing transaction for 1697
          against balance of 15241
age=8204, Bank0 says NEW balance is 16938
age=8207, Depositor3 deposited 1697
age=8209, Depositor3 napping for 785 ms
age=8213, Bank0 processing transaction for 442
          against balance of 16938
age=8334, Depositor2 wants to deposit 469
age=8379, Depositor0 wants to deposit 519
age=8424, Bank0 says NEW balance is 17380
age=8427, Depositor1 deposited 442
age=8429, Depositor1 napping for 1384 ms
age=8433, Bank0 processing transaction for 392
          against balance of 17380
age=8994, Bank0 says NEW balance is 17772
age=8998, Depositor3 wants to deposit 1609
age=9003, Depositor4 deposited 392
age=9005, Depositor4 napping for 1304 ms
age=9008, Bank0 processing transaction for 469
          against balance of 17772
age=9764, Bank0 says NEW balance is 18241
age=9767, Depositor2 deposited 469
age=9769, Depositor2 napping for 342 ms
age=9773, Bank0 processing transaction for 519
          against balance of 18241
age=9834, Depositor1 wants to deposit 1670
age=9974, Bank0 says NEW balance is 18760
age=9977, Depositor0 deposited 519
age=9979, Depositor0 napping for 1993 ms
age=9983, Bank0 processing transaction for 1609
          against balance of 18760
age=10134, Depositor2 wants to deposit 97
age=10324, Depositor4 wants to deposit 936
age()=10415, time to stop the threads and exit

% java DepositWithdraw -s -R10
DepositWithdraw: numBanks=1
 starvationFree=true, debug=false, initialBalance=1000
 numDepositors=2, numWithdrawers=3, dNap=2, wNap=3, runTime=10
age=160, Bank0 is open with initial balance of 1000,
 starvation free is true, allDepositsBeforeAnyWithdrawals=false
age=259, Depositor0 napping for 1753 ms
age=263, Depositor1 napping for 899 ms
age=327, Withdrawer0 napping for 1543 ms
age=331, Withdrawer1 napping for 1852 ms
age=335, Withdrawer2 napping for 758 ms
All threads started
age=1105, Withdrawer2 wants to withdraw 493
age=1115, Bank0 processing allowed withdrawal of -493
          against balance of 1000
age=1246, Depositor1 wants to deposit 294
age=1525, Bank0 says NEW balance is 507
age=1528, Withdrawer2 was able to withdraw 493
age=1531, Withdrawer2 napping for 1061 ms
age=1535, Bank0 processing deposit of 294
          against balance of 507
age=1875, Withdrawer0 wants to withdraw 654
age=1919, Bank0 says NEW balance is 801
age=1922, Depositor1 deposited 294
age=1924, Depositor1 napping for 1431 ms
age=1928, Bank0 processing allowed withdrawal of -654
          against balance of 801
age=2085, Depositor0 wants to deposit 898
age=2175, Withdrawer1 wants to withdraw 493
age=2605, Withdrawer2 wants to withdraw 678
age=2825, Bank0 says NEW balance is 147
age=2827, Withdrawer0 was able to withdraw 654
age=2829, Withdrawer0 napping for 2961 ms
age=2833, Bank0 processing deposit of 898
          against balance of 147
age=3035, Bank0 says NEW balance is 1045
age=3037, Depositor0 deposited 898
age=3039, Depositor0 napping for 1788 ms
age=3043, Bank0 processing allowed withdrawal of -493
          against balance of 1045
age=3375, Depositor1 wants to deposit 305
age=3995, Bank0 says NEW balance is 552
age=3997, Withdrawer1 was able to withdraw 493
age=3999, Withdrawer1 napping for 957 ms
age=4003, Bank0 deferring withdrawal of 678,
 take deposits only to build balance up
age=4008, Bank0 processing nested deposit of 305
          against balance of 552
age=4815, Bank0 says NEW balance is 857
age=4817, Depositor1 deposited 305
age=4820, Depositor1 napping for 557 ms
age=4822, Bank0 processing deferred withdrawal of 678
          against balance of 857
age=4865, Depositor0 wants to deposit 1544
age=4965, Withdrawer1 wants to withdraw 826
age=5065, Bank0 says NEW balance is 179
age=5067, Withdrawer2 was able to withdraw 678
age=5070, Withdrawer2 napping for 2932 ms
age=5073, Bank0 processing deposit of 1544
          against balance of 179
age=5395, Depositor1 wants to deposit 578
age=5685, Bank0 says NEW balance is 1723
age=5687, Depositor0 deposited 1544
age=5689, Depositor0 napping for 1369 ms
age=5693, Bank0 processing allowed withdrawal of -826
          against balance of 1723
age=5815, Withdrawer0 wants to withdraw 1158
age=5860, Bank0 says NEW balance is 897
age=5862, Withdrawer1 was able to withdraw 826
age=5864, Withdrawer1 napping for 694 ms
age=5868, Bank0 processing deposit of 578
          against balance of 897
age=6115, Bank0 says NEW balance is 1475
age=6117, Depositor1 deposited 578
age=6119, Depositor1 napping for 1120 ms
age=6123, Bank0 processing allowed withdrawal of -1158
          against balance of 1475
age=6575, Withdrawer1 wants to withdraw 2196
age=6635, Bank0 says NEW balance is 317
age=6637, Withdrawer0 was able to withdraw 1158
age=6640, Withdrawer0 napping for 1658 ms
age=6643, Bank0 deferring withdrawal of 2196,
 take deposits only to build balance up
age=7075, Depositor0 wants to deposit 1528
age=7078, Bank0 processing nested deposit of 1528
          against balance of 317
age=7245, Depositor1 wants to deposit 458
age=7315, Bank0 says NEW balance is 1845
age=7317, Depositor0 deposited 1528
age=7319, Depositor0 napping for 978 ms
age=7323, Bank0 processing nested deposit of 458
          against balance of 1845
age=7715, Bank0 says NEW balance is 2303
age=7717, Depositor1 deposited 458
age=7719, Depositor1 napping for 249 ms
age=7722, Bank0 processing deferred withdrawal of 2196
          against balance of 2303
age=7985, Depositor1 wants to deposit 825
age=8030, Withdrawer2 wants to withdraw 2459
age=8305, Withdrawer0 wants to withdraw 2296
age=8308, Depositor0 wants to deposit 148
age=8365, Bank0 says NEW balance is 107
age=8367, Withdrawer1 was able to withdraw 2196
age=8370, Withdrawer1 napping for 1536 ms
age=8373, Bank0 processing deposit of 825
          against balance of 107
age=8765, Bank0 says NEW balance is 932
age=8767, Depositor1 deposited 825
age=8769, Depositor1 napping for 1410 ms
age=8773, Bank0 deferring withdrawal of 2459,
 take deposits only to build balance up
age=8778, Bank0 processing nested deposit of 148
          against balance of 932
age=8865, Bank0 says NEW balance is 1080
age=8867, Depositor0 deposited 148
age=8869, Depositor0 napping for 1097 ms
age=9915, Withdrawer1 wants to withdraw 638
age=9975, Depositor0 wants to deposit 495
age=9978, Bank0 processing nested deposit of 495
          against balance of 1080
age=10185, Depositor1 wants to deposit 209
age()=10351, time to stop the threads and exit

% java DepositWithdraw -B3 -R10
DepositWithdraw: numBanks=3
 starvationFree=false, debug=false, initialBalance=1000
 numDepositors=2, numWithdrawers=3, dNap=2, wNap=3, runTime=10
age=162, Bank0 is open with initial balance of 1000,
 starvation free is false, allDepositsBeforeAnyWithdrawals=false
age=267, Bank1 is open with initial balance of 1000,
 starvation free is false, allDepositsBeforeAnyWithdrawals=false
age=315, Bank2 is open with initial balance of 1000,
 starvation free is false, allDepositsBeforeAnyWithdrawals=false
age=331, Depositor0 napping for 537 ms
age=331, Depositor1 napping for 1284 ms
age=347, Withdrawer0 napping for 2399 ms
All threads started
age=396, Withdrawer1 napping for 1948 ms
age=403, Withdrawer2 napping for 1740 ms
age=941, Depositor0 wants to deposit 1422
age=952, Bank0 processing transaction for 1422
          against balance of 1000
age=1721, Depositor1 wants to deposit 251
age=1725, Bank1 processing transaction for 251
          against balance of 1000
age=1921, Bank0 says NEW balance is 2422
age=1929, Depositor0 deposited 1422
age=1931, Depositor0 napping for 1609 ms
age=2181, Withdrawer2 wants to withdraw 2497
age=2371, Withdrawer1 wants to withdraw 2919
age=2671, Bank1 says NEW balance is 1251
age=2674, Depositor1 deposited 251
age=2677, Depositor1 napping for 797 ms
age=2811, Withdrawer0 wants to withdraw 2811
age=3481, Depositor1 wants to deposit 1242
age=3485, Bank2 processing transaction for 1242
          against balance of 1000
age=3541, Depositor0 wants to deposit 1079
age=3545, Bank0 processing transaction for 1079
          against balance of 2422
age=3891, Bank0 says NEW balance is 3501
age=3894, Depositor0 deposited 1079
age=3896, Depositor0 napping for 808 ms
age=3900, Bank0 processing transaction for -2497
          against balance of 3501
age=3971, Bank2 says NEW balance is 2242
age=3974, Depositor1 deposited 1242
age=3976, Depositor1 napping for 169 ms
age=4151, Depositor1 wants to deposit 1321
age=4155, Bank1 processing transaction for 1321
          against balance of 1251
age=4701, Bank0 says NEW balance is 1004
age=4704, Depositor0 wants to deposit 1288
age=4709, Withdrawer2 was able to withdraw 2497
age=4712, Withdrawer2 napping for 1983 ms
age=4716, Bank2 processing transaction for 1288
          against balance of 2242
age=4961, Bank2 says NEW balance is 3530
age=4964, Depositor0 deposited 1288
age=4966, Depositor0 napping for 1132 ms
age=4970, Bank2 processing transaction for -2919
          against balance of 3530
age=5111, Bank1 says NEW balance is 2572
age=5114, Depositor1 deposited 1321
age=5116, Depositor1 napping for 284 ms
age=5411, Depositor1 wants to deposit 1583
age=5415, Bank0 processing transaction for 1583
          against balance of 1004
age=5791, Bank2 says NEW balance is 611
age=5793, Withdrawer1 was able to withdraw 2919
age=5796, Withdrawer1 napping for 276 ms
age=6011, Bank0 says NEW balance is 2587
age=6014, Depositor1 deposited 1583
age=6016, Depositor1 napping for 1724 ms
age=6081, Withdrawer1 wants to withdraw 1356
age=6085, Bank1 processing transaction for -1356
          against balance of 2572
age=6130, Depositor0 wants to deposit 754
age=6133, Bank2 processing transaction for 754
          against balance of 611
age=6411, Bank2 says NEW balance is 1365
age=6414, Depositor0 deposited 754
age=6416, Depositor0 napping for 979 ms
age=6701, Withdrawer2 wants to withdraw 2510
age=6705, Bank0 processing transaction for -2510
          against balance of 2587
age=7061, Bank1 says NEW balance is 1216
age=7064, Withdrawer1 was able to withdraw 1356
age=7066, Withdrawer1 napping for 1409 ms
age=7371, Bank0 says NEW balance is 77
age=7373, Withdrawer2 was able to withdraw 2510
age=7376, Withdrawer2 napping for 1582 ms
age=7420, Depositor0 wants to deposit 1719
age=7423, Bank2 processing transaction for 1719
          against balance of 1365
age=7751, Depositor1 wants to deposit 1973
age=7755, Bank1 processing transaction for 1973
          against balance of 1216
age=8231, Bank2 says NEW balance is 3084
age=8234, Depositor0 deposited 1719
age=8236, Depositor0 napping for 403 ms
age=8239, Bank2 processing transaction for -2811
          against balance of 3084
age=8283, Bank1 says NEW balance is 3189
age=8285, Depositor1 deposited 1973
age=8288, Depositor1 napping for 1717 ms
age=8481, Withdrawer1 wants to withdraw 1349
age=8485, Bank1 processing transaction for -1349
          against balance of 3189
age=8651, Depositor0 wants to deposit 463
age=8655, Bank0 processing transaction for 463
          against balance of 77
age=8721, Bank1 says NEW balance is 1840
age=8724, Withdrawer1 was able to withdraw 1349
age=8726, Withdrawer1 napping for 119 ms
age=8851, Withdrawer1 wants to withdraw 1954
age=8911, Bank2 says NEW balance is 273
age=8914, Withdrawer0 was able to withdraw 2811
age=8916, Withdrawer0 napping for 2211 ms
age=8971, Withdrawer2 wants to withdraw 2108
age=9391, Bank0 says NEW balance is 540
age=9394, Depositor0 deposited 463
age=9396, Depositor0 napping for 1578 ms
age=10011, Depositor1 wants to deposit 1402
age=10015, Bank1 processing transaction for 1402
          against balance of 1840
age()=10432, time to stop the threads and exit
                                            ... end of example run(s)  */
