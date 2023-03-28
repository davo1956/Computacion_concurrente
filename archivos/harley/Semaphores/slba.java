import Utilities.*;
import Synchronization.*;

class Barber extends MyObject implements Runnable {

   private Salon salon = null;

   public Barber(String name, Salon salon) {
      super(name);
      this.salon = salon;
      new Thread(this).start();
   }

   public void run() {
      while (true) {
         salon.wantToCut();
      }
   }
}

class Customer extends MyObject implements Runnable {

   private int id = 0;
   private Salon salon = null;
   private int cutTime = 0;
   private int growTime = 0;

   public Customer(String name, int id, int cutTime,
         int growTime, Salon salon) {
      super(name + " " + id);
      this.id = id;
      this.salon = salon;
      this.cutTime = cutTime;
      this.growTime = growTime;
      new Thread(this).start();
   }

   public void run() {
      int napping;
      while (true) {
         napping = 1 + (int)random(growTime);
         System.out.println("age=" + age()
            + ", " + getName() + " growing hair " + napping + "ms");
         nap(napping);
         System.out.println("age=" + age()
            + ", " + getName() + " needs a haircut");
         salon.wantHairCut(id, cutTime);
      }
   }
}

class Salon extends MyObject {

   private int numChairs = 0;
   private CountingSemaphore customers = null;
   private CountingSemaphore barber = null;
   private BinarySemaphore mutex = null;
   private CountingSemaphore cutting = null;
   private int waiting = 0;

   public Salon(String name, int numChairs) {
      super(name);
      this.numChairs = numChairs;
      customers = new CountingSemaphore(0);
      barber = new CountingSemaphore(0);
      mutex = new BinarySemaphore(1);
      cutting = new CountingSemaphore(0);
   }

   public void wantToCut() {
      System.out.println("age=" + age()
         + ", Barber free, waiting for a customer");
      P(customers);
      P(mutex);
      waiting--;
      V(barber);
      System.out.println("age=" + age()
         + ", Barber has customer, waiting=" + waiting);
      V(mutex);
      System.out.println("age=" + age()
         + ", Barber cutting hair");
      P(cutting);
   }

   public void wantHairCut(int i, int cutTime) {
      int napping;
      P(mutex);
      if (waiting < numChairs) {
         waiting++;
         System.out.println("age=" + age()
            + ", Customer " + i + " in room, waiting=" + waiting);
         V(customers);
         V(mutex);
         P(barber);
         napping = 1 + (int)random(cutTime);
         System.out.println("age=" + age() + ", Customer "
            + i + " getting haircut for " + napping + " ms");
         nap(napping);
         System.out.println("age=" + age()
            + ", Customer " + i + " finished haircut");
         V(cutting);
      } else {
         System.out.println("age=" + age() + ", Customer "
            + i + " room full, waiting=" + waiting);
         V(mutex);
      }
   }
}

class SleepingBarber extends MyObject {

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Us:C:c:g:R:");
      go.optErr = true;
      String usage = "Usage: -s numChairs -C numCustomers"
         + " -c cutTime -g growTime -R runTime";
      int ch = -1;
      int numChairs = 5;
      int numCustomers = 10;
      int cutTime = 2;    // defaults
      int growTime = 4;   // in
      int runTime = 60;   // seconds
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 's') numChairs =
            go.processArg(go.optArgGet(), numChairs);
         else if ((char)ch == 'C') numCustomers =
            go.processArg(go.optArgGet(), numCustomers);
         else if ((char)ch == 'c') cutTime =
            go.processArg(go.optArgGet(), cutTime);
         else if ((char)ch == 'g') growTime =
            go.processArg(go.optArgGet(), growTime);
         else if ((char)ch == 'R')
            runTime = go.processArg(go.optArgGet(), runTime);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("SleepingBarber: numChairs=" + numChairs
         + ", numCustomers=" + numCustomers + ", cutTime=" + cutTime
         + ", growTime=" + growTime + ", runTime=" + runTime);

      // create the Salon object
      Salon salon = new Salon("Salon", numChairs);

      new Barber("Barber", salon); // create the Barber

      // create the Customers (self-starting thread)
      for (int i = 0; i < numCustomers; i++)
         new Customer("Customer", i, cutTime*1000, growTime*1000, salon);
      System.out.println("All Customer threads started");

      // let the Customers run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the Customers and exit");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac slba.java

D:\>java SleepingBarber -U
Usage: -s numChairs -C numCustomers -c cutTime -g growTime -R runTime

D:\>java SleepingBarber -s3 -C6 -c1 -g3 -R5
SleepingBarber: numChairs=3, numCustomers=6, cutTime=1, growTime=3, runTime=5
All Customer threads started
age=110, Barber free, waiting for a customer
age=110, Customer 0 growing hair 2562ms
age=110, Customer 1 growing hair 613ms
age=110, Customer 2 growing hair 2023ms
age=110, Customer 3 growing hair 831ms
age=110, Customer 4 growing hair 765ms
age=110, Customer 5 growing hair 2698ms
age=710, Customer 1 needs a haircut
age=710, Customer 1 in room, waiting=1
age=710, Barber has customer, waiting=0
age=710, Barber cutting hair
age=710, Customer 1 getting haircut for 280 ms
age=880, Customer 4 needs a haircut
age=880, Customer 4 in room, waiting=1
age=990, Customer 3 needs a haircut
age=990, Customer 3 in room, waiting=2
age=1040, Customer 1 finished haircut
age=1040, Customer 1 growing hair 955ms
age=1040, Barber free, waiting for a customer
age=1040, Barber has customer, waiting=1
age=1040, Barber cutting hair
age=1040, Customer 4 getting haircut for 514 ms
age=1590, Customer 4 finished haircut
age=1590, Customer 4 growing hair 1700ms
age=1590, Barber free, waiting for a customer
age=1590, Barber has customer, waiting=0
age=1590, Barber cutting hair
age=1590, Customer 3 getting haircut for 879 ms
age=1980, Customer 1 needs a haircut
age=1980, Customer 1 in room, waiting=1
age=2140, Customer 2 needs a haircut
age=2140, Customer 2 in room, waiting=2
age=2470, Customer 3 finished haircut
age=2470, Customer 3 growing hair 2641ms
age=2470, Barber free, waiting for a customer
age=2470, Barber has customer, waiting=1
age=2470, Barber cutting hair
age=2470, Customer 1 getting haircut for 398 ms
age=2690, Customer 0 needs a haircut
age=2690, Customer 0 in room, waiting=2
age=2850, Customer 5 needs a haircut
age=2850, Customer 5 in room, waiting=3
age=2850, Customer 1 finished haircut
age=2850, Customer 1 growing hair 632ms
age=2850, Barber free, waiting for a customer
age=2850, Barber has customer, waiting=2
age=2850, Barber cutting hair
age=2850, Customer 2 getting haircut for 941 ms
age=3290, Customer 4 needs a haircut
age=3290, Customer 4 in room, waiting=3
age=3510, Customer 1 needs a haircut
age=3510, Customer 1 room full, waiting=3
age=3510, Customer 1 growing hair 1610ms
age=3840, Customer 2 finished haircut
age=3840, Customer 2 growing hair 1317ms
age=3840, Barber free, waiting for a customer
age=3840, Barber has customer, waiting=2
age=3840, Barber cutting hair
age=3840, Customer 0 getting haircut for 354 ms
age=4230, Customer 0 finished haircut
age=4230, Customer 0 growing hair 2603ms
age=4230, Barber free, waiting for a customer
age=4230, Barber has customer, waiting=1
age=4230, Barber cutting hair
age=4230, Customer 5 getting haircut for 313 ms
age=4560, Customer 5 finished haircut
age=4560, Customer 5 growing hair 643ms
age=4560, Barber free, waiting for a customer
age=4560, Barber has customer, waiting=0
age=4560, Barber cutting hair
age=4560, Customer 4 getting haircut for 654 ms
age()=5110, time to stop the Customers and exit
                                            ... end of example run(s)  */
