import java.io.Serializable;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import Utilities.*;

/*
 * To get this example to work on Windows 95, I had to start up dial-up
 * networking, call my ISP, and start PPP.  This eliminated the access
 * exception thrown contacting rmiregistry starting the ComputeServer.
 */

public interface Compute extends Remote {

   public abstract Work compute(Work w) throws RemoteException;
}

class Work extends MyObject implements Serializable {

   private double[] a = null, b = null, c = null;
   private final int N = 5;

   public Work() { this("Work"); }

   public Work(String fromWhom) {
      super("Work from " + fromWhom);
      a = new double[N]; b = new double[N]; c = new double[N];
      for (int i = 0; i < N; i++) {
         a[i] = random(-N, N); b[i] = random(-N, N);
      }
   }

   public void doWork() {
      nap(1+(int)random(1000*N));  // simulate some computation time
      for (int i = 0; i < N; i++) c[i] = a[i] + b[i];
   }

   public String toString() {
      String value = "\n" + getName();
      value += "\na=";
      for (int i = 0; i < N; i++) value += " " + a[i];
      value += "\nb=";
      for (int i = 0; i < N; i++) value += " " + b[i];
      value += "\nc=";
      for (int i = 0; i < N; i++) value += " " + c[i];
      return value;
   }
}

class ComputeServer extends UnicastRemoteObject implements Compute {

   private static String serverName = "ComputeServer";

   private String name = null;
   private static boolean debug = false;
 
   public ComputeServer(String name) throws RemoteException {
      super();
      this.name = name;
   }
    
   public Work compute(Work w) throws RemoteException {
      if (debug) System.out.println(name + " got work request:" + w);
      w.doWork();
      if (debug) System.out.println(name + " sending reply:" + w);
      return w;
   }

   public static void main(String args[])  {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "UdM:");
      go.optErr = true;
      String usage = "Usage: -d -M serverMachine";
      String serverMachine = "cheshire";
      int ch = -1;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'd') debug = true;
         else if ((char)ch == 'M') serverMachine = go.optArgGet();
         else {
            System.err.println(usage);  System.exit(1);
         }
      }

      // install the appropriate security manager
      System.setSecurityManager(new RMISecurityManager());

      // contact rmiregistry
      if (debug) {
         try {
            String[] list = Naming.list("rmi://" + serverMachine);
            System.out.println("list.length=" + list.length);
            for (int i = 0; i < list.length; i++)
               System.out.println("list[" + i + "]=" + list[i]);
         } catch (Exception e) {
            System.err.println(serverName + " exception " + e);
            System.exit(1);
         }
      }

      // register this server
      try {
         ComputeServer server = new ComputeServer(serverName);
         Naming.bind("rmi://" + serverMachine + "/" + serverName, server);
      } catch (Exception e) {
         System.err.println(serverName + " exception " + e);
         System.exit(1);
      }
      System.out.println("server " + serverName + " has been created on "
         + serverMachine + "\n and bound in the registry to the name "
         + serverName);
   }
}

class Client extends MyObject implements Runnable {

   private int id = -1;
   private Compute server = null;
   private int napTime = 0;

   private Client(int id, Compute server, int napTime) {
      super("Client " + id);
      this.id = id;
      this.server = server;
      this.napTime = napTime;
      new Thread(this).start();
   }

   public void run() {
      Work w = null;
      while (true) {
         nap(1 + (int) random(napTime));
         w = new Work(getName());
         System.out.println("age=" + age() + ", "
            + getName() + " sending to server work:" + w);
         try {
            w = server.compute(w);
         } catch (Exception e) {
            System.err.println("Client exception " + e);
            System.exit(1);
         }
         System.out.println("age=" + age() + ", "
            + getName() + " received from server reply:" + w);
      }
   }

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "UM:N:c:n:R:");
      go.optErr = true;
      String usage = "Usage: -M serverMachine -N serverName"
        + " -c numClients -n napTime -R runTime";
      String serverMachine = "cheshire";
      String serverName = "ComputeServer";
      int numClients = 3;
      int napTime = 4;       // both in
      int runTime = 20;      // seconds
      int ch = -1;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'M') serverMachine = go.optArgGet();
         else if ((char)ch == 'N') serverName = go.optArgGet();
         else if ((char)ch == 'c')
            numClients = go.processArg(go.optArgGet(), numClients);
         else if ((char)ch == 'n')
            napTime = go.processArg(go.optArgGet(), napTime);
         else if ((char)ch == 'R')
            runTime = go.processArg(go.optArgGet(), runTime);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("Client: serverMachine=" + serverMachine
         + ", serverName=" + serverName + ", numClients=" + numClients
         + "\n napTime=" + napTime + ", runTime=" + runTime);

      Compute server = null;
      try {
         server = (Compute)
            Naming.lookup("rmi://" + serverMachine + "/" + serverName); 
      } catch (Exception e) {
         System.err.println("Client exception " + e);
         System.exit(1);
      }
      for (int i = 0; i < numClients; i++)
         new Client(i, server, 1000*napTime);

      // let the Clients run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the threads and exit");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

% javac Compute.java
% rmic ComputeServer

% rmiregistry 7777 &
% sleep 5; java ComputeServer -M jubjub:7777 &
server ComputeServer has been created on jubjub:7777
 and bound in the registry to the name ComputeServer

% sleep 5; rsh cheshire "java Client -M jubjub:7777 -R10"
Client: serverMachine=jubjub:7777, serverName=ComputeServer, numClients=3
 napTime=4, runTime=10
age=2017, Client 0 sending to server work:
Work from Client 0
a= -4.08339 2.16857 4.40555 -0.748032 -4.94281
b= 3.84741 1.73436 3.72347 -2.84654 3.26483
c= 0 0 0 0 0
age=4602, Client 1 sending to server work:
Work from Client 1
a= -3.74569 4.42188 1.02479 2.70581 -0.496568
b= -3.19404 -2.76756 -1.57525 4.34111 -3.47821
c= 0 0 0 0 0
age=5262, Client 2 sending to server work:
Work from Client 2
a= -1.67754 -3.87383 -1.19787 -4.46458 4.26563
b= -1.15187 3.56791 -3.88024 0.670651 2.12482
c= 0 0 0 0 0
age=6871, Client 1 received from server reply:
Work from Client 1
a= -3.74569 4.42188 1.02479 2.70581 -0.496568
b= -3.19404 -2.76756 -1.57525 4.34111 -3.47821
c= -6.93972 1.65432 -0.550468 7.04692 -3.97478
age=7291, Client 0 received from server reply:
Work from Client 0
a= -4.08339 2.16857 4.40555 -0.748032 -4.94281
b= 3.84741 1.73436 3.72347 -2.84654 3.26483
c= -0.23598 3.90294 8.12902 -3.59457 -1.67798
age=7504, Client 2 received from server reply:
Work from Client 2
a= -1.67754 -3.87383 -1.19787 -4.46458 4.26563
b= -1.15187 3.56791 -3.88024 0.670651 2.12482
c= -2.82941 -0.30592 -5.07811 -3.79393 6.39045
age=8292, Client 0 sending to server work:
Work from Client 0
a= 0.720275 4.73046 -0.677494 -3.91055 3.09876
b= 1.34022 2.81506 3.91125 4.32273 -0.521754
c= 0 0 0 0 0
age=8811, Client 1 sending to server work:
Work from Client 1
a= 2.92618 0.166923 -3.43393 -4.5423 -4.83467
b= -3.63099 2.5937 1.27995 -0.00706403 2.29606
c= 0 0 0 0 0
age=10180, Client 0 received from server reply:
Work from Client 0
a= 0.720275 4.73046 -0.677494 -3.91055 3.09876
b= 1.34022 2.81506 3.91125 4.32273 -0.521754
c= 2.06049 7.54552 3.23376 0.412184 2.57701
age=10312, Client 2 sending to server work:
Work from Client 2
a= -3.46919 -0.71782 -3.55013 4.81676 2.3632
b= -3.1488 4.15626 1.32766 -1.47453 0.955188
c= 0 0 0 0 0
age()=11460, time to stop the threads and exit
                                            ... end of example run(s)  */
