import java.net.*;
import java.io.*;
import Utilities.*;
import Synchronization.*;

class Message extends MyObject implements Serializable {

   public boolean containsResult = false;
   public boolean containsWork = false;
   public int N = -1;
   public double[] theArray = null;

   public Message(boolean containsResult, boolean containsWork, int N) {
      this.containsResult = containsResult;
      this.containsWork = containsWork;
      if (containsWork && N > 0) {
         theArray = new double[N];
         this.N = N;
         for (int i = 0; i < N; i++) theArray[i] = random(-N, N);
      }
   }

   public String toString() {
      return "  containsResult=" + containsResult + ", containsWork="
         + containsWork + "\n N=" + N + ", a[0]="
         + ((N > 0) ? theArray[0] : 0);
   }
}

class Ping extends MyObject {

   private static boolean debug = false;
   private static boolean localServer = false;
   private static String serverMachine = "localhost";
   private static int portNum = 9999;
   private static int N = 1024;
   private static int M = 1024;
   private static EstablishRendezvous er = null;
   private static long startTime = 0, endTime = 0;

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Udls:p:n:m:");
      go.optErr = true;
      String usage = "Usage: -d -l -s server -p port -n N -m M";
      int ch = -1;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'd') debug = true;
         else if ((char)ch == 'l') localServer = true;
         else if ((char)ch == 's') serverMachine = go.optArgGet();
         else if ((char)ch == 'p')
            portNum = go.processArg(go.optArgGet(), portNum);
         else if ((char)ch == 'n')
            N = go.processArg(go.optArgGet(), N);
         else if ((char)ch == 'm')
            M = go.processArg(go.optArgGet(), M);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      if (portNum < 0) {
         System.err.println("Ping: negative port number, portNum=" + portNum);
         System.exit(1);
      }
      if (M < 1) {
         System.err.println("Ping: trials to do too small, M=" + M);
         System.exit(1);
      }
      if (N < 1) {
         System.err.println("Ping: array too small, N=" + N);
         System.exit(1);
      }

      if (localServer) {  // local case
         System.out.println("local Ping: N=" + N + ", M=" + M
            + ", debug=" + debug + ", localServer=" + localServer);
         er = new EstablishRendezvous();
         new Server(er, debug, localServer);
      } else {            // remote case
         System.out.println("remote Ping: N=" + N + ", M=" + M
            + ", serverMachine=" + serverMachine + ",\n portNum=" + portNum
            + ", debug=" + debug + ", localServer=" + localServer);
         er = new EstablishRendezvous(serverMachine, portNum);
      }

      // send the M "work" requests to the server
      Rendezvous r = null;
      Message m = null;
      startTime = age();
      for (int i = 0; i < M; i++) {
         m = new Message(false, true, N);
         r = er.clientToServer();
         if (debug) System.out.println("age()=" + age()
            + ", Ping sending work m=\n" + m);
         m = (Message) r.clientMakeRequestAwaitReply(m);
         if (debug) System.out.println("age()=" + age() + ", i=" + i
            + ", Ping received result m=\n" + m);
         r.close();
      }
      endTime = age();
      System.out.println("age()=" + age()
         + ", Ping done, average over " + M + " trials of "
         + ((endTime-startTime)/((double)M))
         + " milliseconds\n to send/receive array of " + N  + " doubles");
      m = new Message(false, false, 0);
      r = er.clientToServer();
      if (debug) System.out.println("Ping: sending shutdown to server");
      r.clientMakeRequestAwaitReply(m); // tell remote server to shutdown
      r.close();
      er.close();
      System.exit(0);
   }
}

class Server extends MyObject implements Runnable {

   private EstablishRendezvous er = null;
   private boolean debug = false;
   private boolean localServer = false;

   public Server(EstablishRendezvous er, boolean debug, boolean localServer) {
      super("Server: debug=" + debug + ", localServer=" + localServer);
      this.er = er;
      this.debug = debug;
      this.localServer = localServer;
      if (debug) System.out.println(getName());
      new Thread(this).start();
   }

   public void run() {
      Message m = null;
      while (true) { 
         Rendezvous r = er.serverToClient();
         m = (Message) r.serverGetRequest();
         if (!m.containsWork) {
            if (debug) System.out.println(getName() + " shutdown");
            m = new Message(false, false, 0);
            r.serverMakeReply(m);   // release client
            r.close();
            if (localServer) break; // local client
            else {                  // remote client
               er.close();
               System.exit(0);
            }
         }
         if (debug) System.out.println("age()=" + age() + ", " + getName()
            + " got work, m=\n" + m);
         for (int i = 0; i < m.N; i++) m.theArray[i] += 1;
         m.containsResult = true;  m.containsWork = false;
         r.serverMakeReply(m);
         r.close();
      }
   }

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Udp:");
      go.optErr = true;
      String usage = "Usage: -d -p port";
      int ch = -1;
      int portNum = 9999;
      boolean debug = false;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'd') debug = true;
         else if ((char)ch == 'p')
            portNum = go.processArg(go.optArgGet(), portNum);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      if (portNum < 0) {
         System.err.println("Server: negative portNum=" + portNum);
         System.exit(1);
      }
      System.out.println("Server: portNum=" + portNum + ", debug=" + debug);
      EstablishRendezvous er = new EstablishRendezvous(portNum);
      new Server(er, debug, false);
   }
}

/* ............... Example compile and run(s)

D:\>javac ping.java

D:\>java Ping -d -l -n10 -m2
local Ping: N=10, M=2, debug=true, localServer=true
Server: debug=true, localServer=true
age()=110, Ping sending work m=
  containsResult=false, containsWork=true
 N=10, a[0]=5.50171
age()=110, Server: debug=true, localServer=true got work, m=
  containsResult=false, containsWork=true
 N=10, a[0]=5.50171
age()=110, i=0, Ping received result m=
  containsResult=true, containsWork=false
 N=10, a[0]=6.50171
age()=170, Ping sending work m=
  containsResult=false, containsWork=true
 N=10, a[0]=-0.711345
age()=220, Server: debug=true, localServer=true got work, m=
  containsResult=false, containsWork=true
 N=10, a[0]=-0.711345
age()=220, i=1, Ping received result m=
  containsResult=true, containsWork=false
 N=10, a[0]=0.288655
age()=220, Ping done, average over 2 trials of 80 milliseconds
 to send/receive array of 10 doubles
Ping: sending shutdown to server
Server: debug=true, localServer=true shutdown

D:\>java Ping -l -n1000 -m10
local Ping: N=1000, M=10, debug=false, localServer=true
age()=990, Ping done, average over 10 trials of 94 milliseconds
 to send/receive array of 1000 doubles

D:\>start java Server

D:\>java Ping -n1000 -m10
remote Ping: N=1000, M=10, serverMachine=localhost,
 portNum=9999, debug=false, localServer=false
age()=22570, Ping done, average over 10 trials of 2257 milliseconds
 to send/receive array of 1000 doubles

% javac ping.java

% java Ping -l -n1000 -m10
Java version=1.1_Final, Java vendor=Sun Microsystems Inc.
OS name=Solaris, OS arch=sparc, OS version=2.x
Thu Jul 03 10:44:38 PDT 1997
local Ping: N=1000, M=10, debug=false, localServer=true
age()=214, Ping done, average over 10 trials of 16.7 milliseconds
 to send/receive array of 1000 doubles

% java Server &
Server: portNum=9999, debug=false

% sleep 5; java Ping -n1000 -m10
remote Ping: N=1000, M=10, serverMachine=localhost,
 portNum=9999, debug=false, localServer=false
age()=6304, Ping done, average over 10 trials of 628.7 milliseconds
 to send/receive array of 1000 doubles

% java Server &
Server: portNum=9999, debug=false             // on jubjub

% sleep 5; rsh cheshire "java Ping -s jubjub -n1000 -m10"
remote Ping: N=1000, M=10, serverMachine=jubjub,
 portNum=9999, debug=false, localServer=false
age()=13768, Ping done, average over 10 trials of 1372.2 milliseconds
 to send/receive array of 1000 doubles

% java Server &
Server: portNum=9999, debug=false             // on jubjub

D:\J\>java Ping -s jubjub.mcs.drexel.edu -n1000 -m10
remote Ping: N=1000, M=10, serverMachine=jubjub.mcs.drexel.edu,
 portNum=9999, debug=false, localServer=false
age()=95130, Ping done, average over 10 trials of 9507 milliseconds
 to send/receive array of 1000 doubles
                                            ... end of example run(s)  */
