import java.net.*;
import java.io.*;
import java.util.Date;
import Utilities.*;
import Synchronization.*;

class Message implements Serializable {

   public int workerID = -1;
   public boolean containsResult = false;
   public int numFound = -1;
   public boolean containsWork = false;
   public int N = -1;
   public int inRow = -1;
   public Date date = null;

   public Message(int workerID, boolean containsResult, int numFound,
         boolean containsWork, int inRow, int N) {
      this.workerID = workerID;
      this.containsResult = containsResult;
      this.numFound = numFound;
      this.containsWork = containsWork;
      this.inRow = inRow;
      this.N = N;
      this.date = new Date();
   }

   public String toString() {
      return " workerID=" + workerID + ", containsResult="
         + containsResult + ", numFound=" + numFound + "\n containsWork="
         + containsWork + ", inRow=" +  inRow + ", N=" + N
         + ", date=" + date;
   }
}

class NqueensWorker extends MyObject implements Runnable {

   private int N = -1;
   private String masterMachine = null;
   private int portNum = -1;
   private EstablishRendezvous er = null;
   private int id = -1;

   public NqueensWorker(int id, String masterMachine, int portNum) {
      super("NqueensWorker" + id);
      this.id = id;
      this.masterMachine = masterMachine;
      this.portNum = portNum;
      er = new EstablishRendezvous(masterMachine, portNum);
      new Thread(this).start();
   }

   public NqueensWorker(int id, EstablishRendezvous er) {
      super("NqueensWorker" + id);
      this.id = id;
      this.er = er;
      new Thread(this).start();
   }

   public void run() {
      Message m = new Message(id, false, 0, false, 0, 0);
      while (true) {
         Rendezvous r = er.clientToServer();
         m = (Message) r.clientMakeRequestAwaitReply(m);
         r.close();
         if (!m.containsWork) {
            if (masterMachine != null) { // remote workers
               System.exit(0);
            } else return;
         }
         N = m.N;
         int inRow = m.inRow;
         System.out.println("age()=" + age() + ", " + getName()
            + " counting solutions with column 1 queen in row " + inRow);
         int[] board = new int[N+1];  // need 1 through N (0 unused)
         for (int i = 0; i < board.length; i++) board[i] = 0;
         board[1] = inRow;
         int numFound = place(2, board);
         System.out.println
            ("age()=" + age() + " Solutions with column 1 queen in row "
            + inRow + " = " + numFound);
         m = new Message(id, true, numFound, false, 0, 0);
      }
   }

   private static boolean safe(int row, int column, int[] board) {
      for (int j=1; j<column; j++) {
         if (board[column-j] == row   ||
             board[column-j] == row-j ||
             board[column-j] == row+j) {
            return false;
         }
      }
      return true;
   }

   private static int place(int column, int[] board) {
      int numFound = 0;
      for (int row = 1; row <= board.length-1; row++) {
         board[column] = row;
         if (safe(row, column, board)) {
            if (column==board.length-1) numFound++;
            else numFound += place(column+1, board);
         }
         board[column] = 0;
      }
      return numFound;
   }

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Ui:m:p:");
      go.optErr = true;
      String usage = "Usage: -i id -m master -p port";
      int id = -1;
      String masterMachine = "localhost";
      int portNum = 9999;
      int ch = -1;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'i')
            id = go.processArg(go.optArgGet(), id);
         else if ((char)ch == 'm')
            masterMachine = go.optArgGet();
         else if ((char)ch == 'p')
            portNum = go.processArg(go.optArgGet(), portNum);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("NqueensWorker: id=" + id + ", masterMachine="
         + masterMachine + ", portNum=" + portNum);
      new NqueensWorker(id, masterMachine, portNum);
   }
}

class NqueensMaster extends MyObject {

   private static int N = 8;
   private static int numSolutions = 0;
   private static int portNum = 9999;
   private static EstablishRendezvous er = null;

   public static void main(String[] args) {

      // parse command line options, if any, to override defaults
      GetOpt go = new GetOpt(args, "Uw:n:p:");
      go.optErr = true;
      String usage = "Usage: -w numWorkers -n boardSize -p port";
      int ch = -1;
      int numWorkers = 0;
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 'w')
            numWorkers = go.processArg(go.optArgGet(), numWorkers);
         else if ((char)ch == 'n')
            N = go.processArg(go.optArgGet(), N);
         else if ((char)ch == 'p')
            portNum = go.processArg(go.optArgGet(), portNum);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      if (N < 2) {
         System.out.println("board too small, N=" + N);
         System.exit(1);
      }
      if (numWorkers > 0) {
         er = new EstablishRendezvous();
         System.out.println("NqueensMaster: N=" + N
            + ", numWorkers=" + numWorkers);
         for (int i = 0; i < numWorkers; i++) new NqueensWorker(i, er);
      } else {
         System.out.println("NqueensMaster: N=" + N + ", portNum=" + portNum);
         er = new EstablishRendezvous(portNum);
      }
      // send out all the "work" (initial configurations)
      // a queen in column 1 and row i
      Message m = null;
      int numResultsReceived = 0;
      for (int i = 1; i <= N; i++) {
         Rendezvous r = er.serverToClient();
         m = (Message) r.serverGetRequest();
         if (m.containsResult) {
            numSolutions += m.numFound;
            numResultsReceived++;
            System.out.println("age()=" + age() + " m:" + m);
         }
         r.serverMakeReply(new Message(-1, false, 0, true, i, N));
         r.close();
      }
      // tally up the returning counts
      while (numResultsReceived < N) {
         Rendezvous r = er.serverToClient();
         m = (Message) r.serverGetRequest();
         if (m.containsResult) {
            numSolutions += m.numFound;
            numResultsReceived++;
            System.out.println("age()=" + age() + " m:" + m);
         }
         r.serverMakeReply(new Message(-1, false, 0, false, 0, 0));
         r.close();
      }
      System.out.println("age()=" + age()
         + ", NqueensMaster: numSolutions=" + numSolutions);
      er.close();
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

% javac qumm.java

% java NqueensMaster -n11 &
NqueensMaster: N=11, portNum=9999             // on jubjub

% sleep 5; rsh cheshire "java NqueensWorker -i0 -m jubjub" &
NqueensWorker: id=0, masterMachine=jubjub, portNum=9999
age()=954, NqueensWorker0 counting solutions with column 1 queen in row 1
age()=6094 Solutions with column 1 queen in row 1 = 96
age()=6411, NqueensWorker0 counting solutions with column 1 queen in row 4
age()=12303 Solutions with column 1 queen in row 4 = 295
age()=12561, NqueensWorker0 counting solutions with column 1 queen in row 6
age()=18624 Solutions with column 1 queen in row 6 = 350
age()=18830, NqueensWorker0 counting solutions with column 1 queen in row 9
age()=24984 Solutions with column 1 queen in row 9 = 209
age()=25241, NqueensWorker0 counting solutions with column 1 queen in row 11
age()=30337 Solutions with column 1 queen in row 11 = 96

% sleep 5; rsh bander "java NqueensWorker -i1 -m jubjub"
NqueensWorker: id=1, masterMachine=jubjub, portNum=9999
age()=385, NqueensWorker1 counting solutions with column 1 queen in row 2
age()=3974 Solutions with column 1 queen in row 2 = 219
age()=4233, NqueensWorker1 counting solutions with column 1 queen in row 3
age()=7896 Solutions with column 1 queen in row 3 = 209
age()=8123, NqueensWorker1 counting solutions with column 1 queen in row 5
age()=11894 Solutions with column 1 queen in row 5 = 346
age()=12167, NqueensWorker1 counting solutions with column 1 queen in row 7
age()=15899 Solutions with column 1 queen in row 7 = 346
age()=16143, NqueensWorker1 counting solutions with column 1 queen in row 8
age()=19826 Solutions with column 1 queen in row 8 = 295
age()=20113, NqueensWorker1 counting solutions with column 1 queen in row 10
age()=23660 Solutions with column 1 queen in row 10 = 219

age()=47879 m: workerID=1, containsResult=true, numFound=219
 containsWork=false, inRow=0, N=0, date=Tue Nov 26 16:36:22 EST 1996
age()=48128 m: workerID=0, containsResult=true, numFound=96
 containsWork=false, inRow=0, N=0, date=Tue Nov 26 16:41:05 EST 1996
age()=51797 m: workerID=1, containsResult=true, numFound=209
 containsWork=false, inRow=0, N=0, date=Tue Nov 26 16:36:26 EST 1996
age()=54268 m: workerID=0, containsResult=true, numFound=295
 containsWork=false, inRow=0, N=0, date=Tue Nov 26 16:41:11 EST 1996
age()=55800 m: workerID=1, containsResult=true, numFound=346
 containsWork=false, inRow=0, N=0, date=Tue Nov 26 16:36:30 EST 1996
age()=59812 m: workerID=1, containsResult=true, numFound=346
 containsWork=false, inRow=0, N=0, date=Tue Nov 26 16:36:34 EST 1996
age()=60541 m: workerID=0, containsResult=true, numFound=350
 containsWork=false, inRow=0, N=0, date=Tue Nov 26 16:41:18 EST 1996
age()=63792 m: workerID=1, containsResult=true, numFound=295
 containsWork=false, inRow=0, N=0, date=Tue Nov 26 16:36:37 EST 1996
age()=66938 m: workerID=0, containsResult=true, numFound=209
 containsWork=false, inRow=0, N=0, date=Tue Nov 26 16:41:24 EST 1996
age()=67598 m: workerID=1, containsResult=true, numFound=219
 containsWork=false, inRow=0, N=0, date=Tue Nov 26 16:36:41 EST 1996
age()=72292 m: workerID=0, containsResult=true, numFound=96
 containsWork=false, inRow=0, N=0, date=Tue Nov 26 16:41:29 EST 1996
age()=72366, NqueensMaster: numSolutions=2680

D:\>java NqueensMaster -w3 -n5
Java version=1.1.1, Java vendor=Sun Microsystems Inc.
OS name=Windows 95, OS arch=x86, OS version=4.0
Wed Jul 16 23:39:27 EDT 1997
NqueensMaster: N=5, numWorkers=3
age()=110, NqueensWorker0 counting solutions with column 1 queen in row 1
age()=110 Solutions with column 1 queen in row 1 = 2
age()=110, NqueensWorker1 counting solutions with column 1 queen in row 2
age()=110 Solutions with column 1 queen in row 2 = 2
age()=110 m: workerID=0, containsResult=true, numFound=2
 containsWork=false, inRow=0, N=0, date=Wed Jul 16 23:39:28 EDT 1997
age()=170, NqueensWorker0 counting solutions with column 1 queen in row 3
age()=170 Solutions with column 1 queen in row 3 = 2
age()=170, NqueensWorker2 counting solutions with column 1 queen in row 4
age()=170 Solutions with column 1 queen in row 4 = 2
age()=170 m: workerID=1, containsResult=true, numFound=2
 containsWork=false, inRow=0, N=0, date=Wed Jul 16 23:39:28 EDT 1997
age()=170, NqueensWorker1 counting solutions with column 1 queen in row 5
age()=170 Solutions with column 1 queen in row 5 = 2
age()=170 m: workerID=0, containsResult=true, numFound=2
 containsWork=false, inRow=0, N=0, date=Wed Jul 16 23:39:28 EDT 1997
age()=220 m: workerID=2, containsResult=true, numFound=2
 containsWork=false, inRow=0, N=0, date=Wed Jul 16 23:39:28 EDT 1997
age()=220 m: workerID=1, containsResult=true, numFound=2
 containsWork=false, inRow=0, N=0, date=Wed Jul 16 23:39:28 EDT 1997
age()=220, NqueensMaster: numSolutions=10
                                            ... end of example run(s)  */
