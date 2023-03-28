import Utilities.*;
import Synchronization.*;

class RendezvousRequestReply extends MyObject {

   private double[] a = null, b = null, c = null;
   private final int N = 5;
   private String fromWhom = null;

   public RendezvousRequestReply(String fromWhom) {
      super("RendezvousRequestReply");
      this.fromWhom = fromWhom;
      a = new double[N]; b = new double[N]; c = new double[N];
      for (int i = 0; i < N; i++) {
         a[i] = random(-N, N); b[i] = random(-N, N);
      }
   }

   public void doRequest() {
      nap(1+(int)random(1000*N));  // simulate some computation time
      for (int i = 0; i < N; i++) c[i] = a[i] + b[i];
   }

   public String toString() {
      String value = "\nfromWhom=" + fromWhom;
      value += "\na=";
      for (int i = 0; i < N; i++) value += " " + a[i];
      value += "\nb=";
      for (int i = 0; i < N; i++) value += " " + b[i];
      value += "\nc=";
      for (int i = 0; i < N; i++) value += " " + c[i];
      return value;
   }

   public String fromWhom() { return fromWhom; }
}

class Client extends MyObject implements Runnable {

   private int id = -1;
   private int napTime = 0;
   private EstablishRendezvous er = null;

   public Client(int id, int napTime, EstablishRendezvous er) {
      super("client " + id);
      this.id = id;
      this.napTime = napTime;
      this.er = er;
      System.out.println(getName() + " is alive, napTime=" + napTime);
      new Thread(this).start();
   }

   public void run() {
      int napping;
      RendezvousRequestReply rrr;
      while (true) {
         napping = 1 + (int)random(napTime);
         nap(napping);
         rrr = new RendezvousRequestReply(getName());
         System.out.println("age()=" + age() + ", " + getName()
            + " wants to rendezvous");
/*
 * In the following statement, we could discard the return value since the
 * server made its changes inside rrr and we are in the same JVM as the server
 * so the object reference we get back is to the same actual object we sent.
 * BUT if we ever change the program to the remote case where the client and
 * server are in different JVMs, then we would have to change the following
 * statement.  To avoid that future potential bug, we will store the return
 * value back into rrr, which works for both local and remote.
 */
         rrr = (RendezvousRequestReply)
            er.clientToServer().clientMakeRequestAwaitReply(rrr);
         System.out.println("age()=" + age() + ", " + getName()
            + " after rendezvous" + rrr);
      }
   }
}

class ServerThread extends MyObject implements Runnable {

   private Rendezvous r = null;

   public ServerThread(Rendezvous r) {
      super("ServerThread");
      this.r = r;
      new Thread(this).start();
   }

   public void run() {
      RendezvousRequestReply rrr
         = (RendezvousRequestReply) r.serverGetRequest();
      System.out.println("age()=" + age()
         + ", a ServerThread got a request from " + rrr.fromWhom());
      rrr.doRequest();
      System.out.println("age()=" + age()
         + ", a ServerThread finished the request from " + rrr.fromWhom());
      r.serverMakeReply(rrr);
   }
}

class MultiThreadedServer extends MyObject implements Runnable {

   private EstablishRendezvous er = null;
   private boolean threadedServer = false;

   public MultiThreadedServer(EstablishRendezvous er,
         boolean threadedServer) {
      super("MultiThreadedServer");
      this.er = er;
      this.threadedServer = threadedServer;
      System.out.println(getName() + " is alive");
      new Thread(this).start();
   }

   public void run() {
      while (true) {
         Rendezvous r = er.serverToClient();
         if (threadedServer) {
            System.out.println("age()=" + age() + ", Server got a request");
            // spawn a new thread to handle the request asynchronously
            new ServerThread(r);
         } else {  // do it here and now before handling any more clients
            RendezvousRequestReply rrr
               = (RendezvousRequestReply) r.serverGetRequest();
            System.out.println("age()=" + age()
               + ", Server got a request from " + rrr.fromWhom());
            rrr.doRequest();
            System.out.println("age()=" + age()
               + ", Server finished the request from " + rrr.fromWhom());
            r.serverMakeReply(rrr);
         }
      }
   }
}

class ClientServer extends MyObject {

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Utc:n:R:");
      go.optErr = true;
      String usage = "Usage: -t -c numClients -n napTime -R runTime";
      int ch = -1;
      boolean threadedServer = false;
      int numClients = 3;
      int napTime = 4;
      int runTime = 20;      // seconds
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 't') threadedServer = true;
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
      System.out.println("ClientServer: numClients=" + numClients
         + ", napTime=" + napTime + ", runTime=" + runTime);
      if (threadedServer)
         System.out.println
            ("Each client request will be handled in its own server thread");

      // create the server
      EstablishRendezvous er = new EstablishRendezvous();
      MultiThreadedServer s = new MultiThreadedServer(er, threadedServer);

      // create the Clients
      for (int i = 0; i < numClients; i++) new Client(i, 1000*napTime, er);

      // let the Clients run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the threads and exit");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac clsv.java

D:\>java ClientServer -c4 -R15
ClientServer: numClients=4, napTime=4, runTime=15
MultiThreadedServer is alive
client 0 is alive, napTime=4000
client 1 is alive, napTime=4000
client 2 is alive, napTime=4000
client 3 is alive, napTime=4000
age()=170, client 0 wants to rendezvous
age()=280, Server got a request from client 0
age()=830, Server finished the request from client 0
age()=830, client 0 after rendezvous
fromWhom=client 0
a= 0.176308 -3.65113 1.90081 -0.351813 -4.85627
b= 2.97069 4.57178 3.25911 -4.1121 -3.60055
c= 3.147 0.920648 5.15992 -4.46391 -8.45682
age()=880, client 1 wants to rendezvous
age()=880, Server got a request from client 1
age()=1260, client 3 wants to rendezvous
age()=1980, client 0 wants to rendezvous
age()=2030, client 2 wants to rendezvous
age()=5710, Server finished the request from client 1
age()=5710, client 1 after rendezvous
fromWhom=client 1
a= 2.20636 0.22274 -4.80148 3.46159 -4.07722
b= -0.395222 -4.86496 0.661616 -0.734338 1.17361
c= 1.81114 -4.64222 -4.13986 2.72725 -2.90362
age()=5770, Server got a request from client 3
age()=8290, client 1 wants to rendezvous
age()=9610, Server finished the request from client 3
age()=9610, client 3 after rendezvous
fromWhom=client 3
a= -2.47209 -2.53815 1.53533 -3.71705 0.608039
b= -4.5575 -3.35972 -4.22435 1.70403 1.71254
c= -7.02959 -5.89787 -2.68902 -2.01302 2.32058
age()=9610, Server got a request from client 0
age()=11040, client 3 wants to rendezvous
age()=12140, Server finished the request from client 0
age()=12140, client 0 after rendezvous
fromWhom=client 0
a= 0.972876 -2.60852 -0.115246 -3.27382 3.06074
b= 1.83023 1.62499 2.28122 2.3162 -0.993425
c= 2.80311 -0.983532 2.16597 -0.957624 2.06732
age()=12190, Server got a request from client 2
age()=13240, client 0 wants to rendezvous
age()=14940, Server finished the request from client 2
age()=14940, client 2 after rendezvous
fromWhom=client 2
a= -4.45033 4.96301 1.15257 0.786161 -2.53553
b= 4.45445 3.62493 1.94064 2.83647 0.678649
c= 0.00411965 8.58794 3.09321 3.62263 -1.85688
age()=14940, Server got a request from client 1
age()=15160, time to stop the threads and exit

D:\>java ClientServer -t -c4 -R10
ClientServer: numClients=4, napTime=4, runTime=10
Each client request will be handled in its own server thread
MultiThreadedServer is alive
client 0 is alive, napTime=4000
client 1 is alive, napTime=4000
client 2 is alive, napTime=4000
client 3 is alive, napTime=4000
age()=930, client 3 wants to rendezvous
age()=930, Server got a request
age()=930, a ServerThread got a request from client 3
age()=1700, client 2 wants to rendezvous
age()=1700, Server got a request
age()=1700, a ServerThread got a request from client 2
age()=2800, client 0 wants to rendezvous
age()=2800, Server got a request
age()=2800, a ServerThread got a request from client 0
age()=3460, a ServerThread finished the request from client 2
age()=3460, client 2 after rendezvous
fromWhom=client 2
a= 3.47112 3.35398 -4.82645 -1.94492 -4.87518
b= -3.00398 0.173401 -2.38435 2.06435 4.58692
c= 0.467137 3.52738 -7.2108 0.11943 -0.288263
age()=3620, client 1 wants to rendezvous
age()=3620, Server got a request
age()=3620, a ServerThread got a request from client 1
age()=4280, a ServerThread finished the request from client 3
age()=4280, client 3 after rendezvous
fromWhom=client 3
a= -4.61478 -4.49431 0.426164 3.21181 0.756048
b= -4.54147 3.99372 -4.00696 2.89895 0.649383
c= -9.15625 -0.500589 -3.5808 6.11076 1.40543
age()=6210, client 2 wants to rendezvous
age()=6210, Server got a request
age()=6210, a ServerThread got a request from client 2
age()=6590, a ServerThread finished the request from client 2
age()=6590, client 2 after rendezvous
fromWhom=client 2
a= 4.87003 -4.63748 4.69299 -3.58343 2.96334
b= 2.32726 -3.63721 0.914748 -2.68447 1.90788
c= 7.19729 -8.27468 5.60774 -6.2679 4.87121
age()=7250, a ServerThread finished the request from client 0
age()=7250, client 0 after rendezvous
fromWhom=client 0
a= -1.36603 4.02213 2.92174 4.54016 -3.53111
b= -1.18313 -0.0211466 4.63714 -2.46519 -3.89137
c= -2.54917 4.00099 7.55889 2.07497 -7.42248
age()=8020, client 3 wants to rendezvous
age()=8020, Server got a request
age()=8020, a ServerThread got a request from client 3
age()=8020, client 2 wants to rendezvous
age()=8020, Server got a request
age()=8020, a ServerThread got a request from client 2
age()=8400, a ServerThread finished the request from client 1
age()=8400, client 1 after rendezvous
fromWhom=client 1
a= -0.738761 -2.9057 3.12267 -1.12499 -1.47475
b= 1.55269 1.43304 -0.516861 3.31844 -0.544111
c= 0.813931 -1.47266 2.60581 2.19345 -2.01886
age()=9010, a ServerThread finished the request from client 2
age()=9010, client 2 after rendezvous
fromWhom=client 2
a= -0.0128668 4.16441 -2.20455 -1.08956 -1.67223
b= -2.4696 3.96719 -0.51026 -2.8545 3.19811
c= -2.48247 8.13161 -2.71481 -3.94405 1.52588
age()=10050, time to stop the threads and exit
                                            ... end of example run(s)  */
