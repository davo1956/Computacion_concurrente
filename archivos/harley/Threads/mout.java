import java.io.*;
import Utilities.*;

class Helper extends MyObject implements Runnable {

   private int id = 0;
   private int napTime = 0; // milliseconds
   private PrintWriter outFile = null;

   public Helper(String name, int id, int napTime, PrintWriter outFile) {
      super(name + " " + id);
      this.id = id;
      this.napTime = napTime;
      this.outFile = outFile;
      outFile.println(getName() + " is alive, napTime="
         + napTime);
   }

   public void run() {
      int napping;
      while (true) {
         napping = ((int) random(napTime)) + 1;
         outFile.println("age()=" + age() + ", " + getName()
            + " napping for " + napping + " ms");
         nap(napping);
      }
   }
}

class HelperThreads extends MyObject {

   public static void main(String[] args) {

      // parse command line arguments, if any, to override defaults
      GetOpt go = new GetOpt(args, "Uh:n:F:R:");
      go.optErr = true;
      String usage = "Usage: -h numHelpers -n napTime -F file -R runTime";
      int ch = -1;
      int numHelpers = 4;
      int napTime = 3;    // defaults
      int runTime = 60;   // in seconds
      String outFileName = "helper";
      while ((ch = go.getopt()) != go.optEOF) {
         switch ((char) ch) {
            case 'U':
               System.out.println(usage);  System.exit(0);
               break;
            case 'h':
               numHelpers = go.processArg(go.optArgGet(), numHelpers);
               break;
            case 'n':
               napTime = go.processArg(go.optArgGet(), napTime);
               break;
            case 'F':
               outFileName = go.optArgGet();
               break;
            case 'R':
               runTime = go.processArg(go.optArgGet(), runTime);
               break;
            default:
               System.err.println(usage);  System.exit(1);
               break;
         }
      }
      System.out.println("HelperThreads: numHelpers=" + numHelpers
         + ", napTime=" + napTime + ", runTime=" + runTime
         + ", outFileName=" + outFileName);

      // start the Helper threads
      Thread[] helper = new Thread[numHelpers];
      PrintWriter[] outFile = new PrintWriter[numHelpers];
      for (int i = 0; i < numHelpers; i++) {
         String fileName = outFileName + i + ".out";
         boolean autoFlush = true;
         try {
            outFile[i] = new PrintWriter(new BufferedWriter(
               new FileWriter(new File(fileName))), autoFlush);
         } catch (IOException e) {
            System.err.println("IOException opening file " + fileName);
            System.exit(1);
         }
         helper[i] = new Thread(
            new Helper("Helper", i, napTime*1000, outFile[i]));
      }
      for (int i = 0; i < numHelpers; i++) helper[i].start();
      System.out.println("All Helper threads started");

      // let the Helpers run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the threads and exit");
      for (int i = 0; i < numHelpers; i++) {
         helper[i].stop();
         outFile[i].close();
      }
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

D:\>javac mout.java

D:\>java HelperThreads -U
Usage: -h numHelpers -n napTime -F file -R runTime

D:\>java HelperThreads -R5
HelperThreads: numHelpers=4, napTime=3, runTime=5, outFileName=helper
All Helper threads started
age()=5160, time to stop the threads and exit

D:\>dir *.out
HELPER0  OUT           152  07-25-96  1:44p helper0.out
HELPER1  OUT           192  07-25-96  1:44p helper1.out
HELPER2  OUT           154  07-25-96  1:44p helper2.out
HELPER3  OUT           113  07-25-96  1:44p helper3.out

D:\>type helper2.out
Helper 2 is alive, napTime=3000
age()=110, Helper 2 napping for 2185 ms
age()=2470, Helper 2 napping for 1157 ms
age()=3630, Helper 2 napping for 2602 ms
                                            ... end of example run(s)  */
