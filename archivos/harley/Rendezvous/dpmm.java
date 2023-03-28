import java.net.*;
import java.io.*;
import Utilities.*;
import Synchronization.*;

class Philosopher extends MyObject implements Runnable {

   private static int id = 0;
   private static int napThink = 0; // both are in
   private static int napEat = 0;   // seconds
   private static Rendezvous r = null;

   private Philosopher(String name) {
      super(name);
      new Thread(this).start();
   }

   private void think() {
      int napping;
      napping = 1 + (int) random(1000*napThink);
      System.out.println("age()=" + age() + ", " + getName()
         + " is thinking for " + napping + " ms");
      nap(napping);
   }

   private void eat() {
      int napping;
      napping = 1 + (int) random(1000*napEat);
      System.out.println("age()=" + age() + ", " + getName()
         + " is eating for " + napping + " ms");
      nap(napping);
   }

   public void run() {
      while (true) {
         think();
         System.out.println("age()=" + age() + ", " + getName()
            + " wants to eat");
         try {
            r.clientMakeRequestAwaitReply(new Integer(id)); // takeForks(id);
            eat();
            r.clientMakeRequestAwaitReply(new Integer(-id-1));// putForks(id);
         } catch (MessagePassingException e) {
            System.err.println("age()=" + age() + ", " + getName()
               + ":" + e);
            r.close();
            System.exit(1);
         }
      }
   }

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Ui:t:e:s:p:");
      go.optErr = true;
      String usage = "Usage: -i id -t napThink -e napEat -s server -p port";
      int ch = -1;
      id = 0;
      napThink = 8;  // defaults are
      napEat = 2;    // both in seconds
      String diningServer = "localhost";
      int port = 9999;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'i')
            id = go.processArg(go.optArgGet(), id);
         else if ((char)ch == 't')
            napThink = go.processArg(go.optArgGet(), napThink);
         else if ((char)ch == 'e')
            napEat = go.processArg(go.optArgGet(), napEat);
         else if ((char)ch == 's')
            diningServer = go.optArgGet();
         else if ((char)ch == 'p')
            port = go.processArg(go.optArgGet(), port);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("Philosopher: id=" + id + " napThink="
         + napThink + " napEat=" + napEat + " diningServer="
         + diningServer + " port=" + port);

      // create a rendezvous to the DiningServer object
      EstablishRendezvous er = null;
      try {
         er = new EstablishRendezvous(diningServer, port);
         r = er.clientToServer();
         System.out.println("Connected to " + diningServer
            + " on port " + port);
      } catch (MessagePassingException e) {
         System.err.println("Philosopher id=" + id + " e=" + e);
         System.exit(1);
      }
      // tell the dining server that this philosopher is alive
      r.clientMakeRequestAwaitReply(new Integer(id));

      // start the Philosopher thread
      new Philosopher("Philosopher " + id);
      System.out.println("Philosopher thread " + id + " started");
   }
}

class DiningServer extends MyObject implements Runnable {

   private static final int
      THINKING = 0, HUNGRY = 1, EATING = 2;
   private static int numPhils = 0;
   private static int[] state = null;
   private static BinarySemaphore[] self = null;
   private static BinarySemaphore mutex = null;

   private int philosopherID = -1;
   private Rendezvous r = null;

   private DiningServer(int philosopherID, Rendezvous r) {
      this.philosopherID = philosopherID;
      this.r = r;
      new Thread(this).start();
   }

   private static final int left(int i) { return (numPhils+i-1) % numPhils; }

   private static final int right(int i) { return (i+1) % numPhils; }

   private static void takeForks(int i) {
      P(mutex);
      state[i] = HUNGRY;
      test(i);
      V(mutex);
      P(self[i]);
   }

   private static void putForks(int i) {
      P(mutex);
      state[i] = THINKING;
      test(left(i));
      test(right(i));
      V(mutex);
   }

   private static void test(int k) {
      if (state[left(k)] != EATING && state[right(k)] != EATING
            && state[k] == HUNGRY) {
         state[k] = EATING;
         V(self[k]);
      }
   }

   public void run() {
      int message = 0;
      while (true) {
         System.out.println("age=" + age()
            + " waiting for message from philosopher " + philosopherID);
         message = ((Integer) r.serverGetRequest()).intValue();
         if (message == -philosopherID-1) {
            System.out.println("age=" + age()
               + " putForks from philosopher " + philosopherID);
            putForks(philosopherID);
            // acknowledge the release of the forks
            r.serverMakeReply(new Integer(0));
         } else if (message == philosopherID) {
            System.out.println("age=" + age()
               + " takeForks from philosopher " + philosopherID);
            takeForks(philosopherID);
            System.out.println("age=" + age()
               + " forks available for philosopher " + philosopherID);
            // release the philosopher when forks available
            r.serverMakeReply(new Integer(0));
         } else {
            System.err.println("philosopherID=" + philosopherID
               + ", illegal message=" + message);
         }
      }
   }

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Un:p:R:");
      go.optErr = true;
      String usage = "Usage: -n numPhils -p port -R runTime";
      int ch = -1;
      numPhils = 5;   // default
      int port = 9999;       // values
      int runTime = 60;      // seconds
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'n')
             numPhils = go.processArg(go.optArgGet(), numPhils);
         else if ((char)ch == 'p')
            port = go.processArg(go.optArgGet(), port);
         else if ((char)ch == 'R')
            runTime = go.processArg(go.optArgGet(), runTime);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      state = new int[numPhils];
      for (int i = 0; i < numPhils; i++) state[i] = THINKING;
      self = new BinarySemaphore[numPhils];
      for (int i = 0; i < numPhils; i++) self[i] = new BinarySemaphore(0);
      mutex = new BinarySemaphore(1);
      System.out.println("DiningServer: numPhils="
         + numPhils + ", port=" + port + ", runTime=" + runTime);

      // accept connections from the philosophers
      // and start a message passing thread for each philosopher
      EstablishRendezvous er = null;
      try {
         er = new EstablishRendezvous(port);
         System.out.println("Waiting for connection on port " + port);
      } catch (MessagePassingException e) {
         System.err.println("DiningServer:" + e);  System.exit(1);
      }
      Rendezvous[] r = new Rendezvous[numPhils];
      for (int i = 0; i < numPhils; i++) {
         try {
            r[i] = er.serverToClient();
         } catch (MessagePassingException e) {
            System.err.println("DiningServer:" + e);
         }
         int id = ((Integer) r[i].serverGetRequest()).intValue();
         new DiningServer(id, r[i]);
         System.out.println("Accepted connection from philosopher " + id);
      }
      System.out.println("DiningServer: all philosophers connected");
      for (int i = 0; i < numPhils; i++) // let the philosophers start eating
         r[i].serverMakeReply(new Integer(0));

      // let the Philosophers run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the Philosophers and exit");
      for (int i = 0; i < numPhils; i++) r[i].close();
      er.close();
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

% javac dpmm.java

% java DiningServer -n5 -R20 &
DiningServer: numPhils=5, port=9999, runTime=20  // on jubjub
Waiting for connection on port 9999

% sleep 5; rsh bander "java Philosopher -s jubjub -i0" &
Philosopher: id=0 napThink=8 napEat=2 diningServer=jubjub port=9999
Connected to jubjub on port 9999
Accepted connection from philosopher 0
age=5880 waiting for message from philosopher 0

% sleep 5; rsh cheshire "java Philosopher -s jubjub -i1" &
Philosopher: id=1 napThink=8 napEat=2 diningServer=jubjub port=9999
Connected to jubjub on port 9999
Accepted connection from philosopher 1
age=9500 waiting for message from philosopher 1

% sleep 5; rsh humpty "java Philosopher -s jubjub -i2" &
Philosopher: id=2 napThink=8 napEat=2 diningServer=jubjub port=9999
Connected to jubjub on port 9999
Accepted connection from philosopher 2
age=12740 waiting for message from philosopher 2

% sleep 5; rsh queen "java Philosopher -s jubjub -i3" &
Philosopher: id=3 napThink=8 napEat=2 diningServer=jubjub port=9999
Connected to jubjub on port 9999
Accepted connection from philosopher 3
age=15870 waiting for message from philosopher 3

% sleep 5; rsh king "java Philosopher -s jubjub -i4"
Philosopher: id=4 napThink=8 napEat=2 diningServer=jubjub port=9999
Connected to jubjub on port 9999
Accepted connection from philosopher 4
age=18780 waiting for message from philosopher 4
DiningServer: all philosophers connected         // Philosopher output deleted
age=20100 takeForks from philosopher 4           // from here on
age=20100 forks available for philosopher 4
age=20100 waiting for message from philosopher 4
age=20430 takeForks from philosopher 2
age=20430 forks available for philosopher 2
age=20430 waiting for message from philosopher 2
age=21530 putForks from philosopher 4
age=21530 waiting for message from philosopher 4
age=22190 putForks from philosopher 2
age=22190 waiting for message from philosopher 2
age=23070 takeForks from philosopher 4
age=23070 forks available for philosopher 4
age=23070 waiting for message from philosopher 4
age=24170 putForks from philosopher 4
age=24170 waiting for message from philosopher 4
age=26310 takeForks from philosopher 2
age=26310 forks available for philosopher 2
age=26310 waiting for message from philosopher 2
age=26690 takeForks from philosopher 0
age=26690 forks available for philosopher 0
age=26690 waiting for message from philosopher 0
age=26750 takeForks from philosopher 3
age=27410 takeForks from philosopher 1
age=27900 putForks from philosopher 2
age=27900 forks available for philosopher 3
age=27900 waiting for message from philosopher 3
age=27960 waiting for message from philosopher 2
age=28510 putForks from philosopher 3
age=28510 waiting for message from philosopher 3
age()=28830, time to stop the Philosophers and exit
                                            ... end of example run(s)  */
