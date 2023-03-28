import Utilities.*;
import Synchronization.*;

class Buffer { public String who; public double value; public long when;
   public Buffer() {who = null; value = 0.0; when = 0;}
   public String toString()
      {return " who="+ who + " value=" + value + " when=" + when;}
}

class Producer extends MyObject implements Runnable {

   private int pNap = 0; // milliseconds
   private MessagePassing mpEmpty = null, mpFull = null;

   public Producer(String name, int pNap,
         MessagePassing mpEmpty, MessagePassing mpFull) {
      super(name);
      this.pNap = pNap;
      this.mpEmpty = mpEmpty;
      this.mpFull = mpFull;
      new Thread(this).start();
   }

   public void run() {
      int napping;  double value;
      while (true) {
         napping = 1 + (int) random(pNap);
         System.out.println("age=" + age() + ", " + getName()
            + " napping for " + napping + " ms");
         nap(napping);
         value = random();
         System.out.println("age=" + age() + ", " + getName()
            + " produced value " + value);
         Buffer buffer = (Buffer) receive(mpEmpty);
         buffer.who = getName(); buffer.value = value; buffer.when = age();
         send(mpFull, buffer);
         System.out.println("age=" + age() + ", " + getName()
            + " deposited value " + value);
      }
   }
}

class Consumer extends MyObject implements Runnable {

   private int cNap = 0; // milliseconds
   private MessagePassing mpEmpty = null, mpFull = null;

   public Consumer(String name, int cNap,
         MessagePassing mpEmpty, MessagePassing mpFull) {
      super(name);
      this.cNap = cNap;
      this.mpEmpty = mpEmpty;
      this.mpFull = mpFull;
      new Thread(this).start();
   }

   public void run() {
      int napping;
      while (true) {
         napping = 1 + (int) random(cNap);
         System.out.println("age=" + age() + ", " + getName()
            + " napping for " + napping + " ms");
         nap(napping);
         System.out.println("age=" + age() + ", " + getName()
            + " wants to consume");
         Buffer buffer = (Buffer) receive(mpFull);
         System.out.println("age=" + age() + ", " + getName()
            + " fetched buffer" + buffer);
         buffer.who = null; buffer.value = 0.0; buffer.when = 0;
         send(mpEmpty, buffer);
      }
   }
}

class ProducerConsumer extends MyObject {

   public static void main(String[] args) {

      // parse command line arguments, if any, to override defaults
      GetOpt go = new GetOpt(args, "Us:p:c:R:");
      go.optErr = true;
      String usage = "Usage: -s numSlots -p pNap -c cNap -R runTime";
      int ch = -1;
      int numSlots = 20;
      int pNap = 3;       // defaults
      int cNap = 2;       // in
      int runTime = 60;   // seconds
      while ((ch = go.getopt()) != go.optEOF) {
         if      ((char)ch == 'U') {
            System.out.println(usage);  System.exit(0);
         }
         else if ((char)ch == 's')
            numSlots = go.processArg(go.optArgGet(), numSlots);
         else if ((char)ch == 'p')
            pNap = go.processArg(go.optArgGet(), pNap);
         else if ((char)ch == 'c')
            cNap = go.processArg(go.optArgGet(), cNap);
         else if ((char)ch == 'R')
            runTime = go.processArg(go.optArgGet(), runTime);
         else {
            System.err.println(usage);  System.exit(1);
         }
      }
      System.out.println("ProducerConsumer"
         + ", numSlots=" + numSlots
         + ", pNap=" + pNap + ", cNap=" + cNap + ", runTime=" + runTime);

      // create the kind of message passing
      MessagePassing mpEmpty = new AsyncMessagePassing();
      MessagePassing mpEmptyS = new MessagePassingSendOnly(mpEmpty);
      MessagePassing mpEmptyR = new MessagePassingReceiveOnly(mpEmpty);
      MessagePassing mpFull = new AsyncMessagePassing();
      MessagePassing mpFullS = new MessagePassingSendOnly(mpFull);
      MessagePassing mpFullR = new MessagePassingReceiveOnly(mpFull);

      // create the Producer and Consumer (they start their own threads)
      new Producer("PRODUCER", pNap*1000, mpEmptyR, mpFullS);
      new Consumer("Consumer", cNap*1000, mpEmptyS, mpFullR);
      System.out.println("All threads started");

      // set up the bounded buffer with numSlots empty messages
      for (int i = 0; i < numSlots; i++) send(mpEmpty, new Buffer());

      // let them run for a while
      nap(runTime*1000);
      System.out.println("age()=" + age()
         + ", time to stop the threads and exit");
      System.exit(0);
   }
}

/* ............... Example compile and run(s)

% javac bbmp.java

% java ProducerConsumer -s3 -p1 -c3 -R6
ProducerConsumer, numSlots=3, pNap=1, cNap=3, runTime=6
age=71, PRODUCER napping for 914 ms
age=124, Consumer napping for 2765 ms
All threads started
age=1046, PRODUCER produced value 0.0562537
age=1057, PRODUCER deposited value 0.0562537
age=1060, PRODUCER napping for 679 ms
age=1746, PRODUCER produced value 0.956799
age=1749, PRODUCER deposited value 0.956799
age=1752, PRODUCER napping for 749 ms
age=2506, PRODUCER produced value 0.815233
age=2509, PRODUCER deposited value 0.815233
age=2512, PRODUCER napping for 106 ms
age=2631, PRODUCER produced value 0.759821
age=2986, Consumer wants to consume
age=2988, Consumer fetched buffer who=PRODUCER value=0.0562537 when=1056
age=2994, PRODUCER deposited value 0.759821
age=2996, PRODUCER napping for 1000 ms
age=2998, Consumer napping for 2230 ms
age=4006, PRODUCER produced value 0.510975
age=5226, Consumer wants to consume
age=5228, Consumer fetched buffer who=PRODUCER value=0.956799 when=1749
age=5232, PRODUCER deposited value 0.510975
age=5234, PRODUCER napping for 936 ms
age=5237, Consumer napping for 1782 ms
age=6186, PRODUCER produced value 0.052863
age()=6229, time to stop the threads and exit

% java ProducerConsumer -s3 -p3 -c1 -R6
ProducerConsumer, numSlots=3, pNap=3, cNap=1, runTime=6
age=70, PRODUCER napping for 668 ms
age=124, Consumer napping for 527 ms
All threads started
age=744, Consumer wants to consume
age=804, PRODUCER produced value 0.0799026
age=813, Consumer fetched buffer who=PRODUCER value=0.0799026 when=813
age=819, Consumer napping for 25 ms
age=822, PRODUCER deposited value 0.0799026
age=825, PRODUCER napping for 2920 ms
age=944, Consumer wants to consume
age=3734, PRODUCER produced value 0.609787
age=3737, Consumer fetched buffer who=PRODUCER value=0.609787 when=3737
age=3741, Consumer napping for 690 ms
age=3744, PRODUCER deposited value 0.609787
age=3747, PRODUCER napping for 471 ms
age=4234, PRODUCER produced value 0.64816
age=4236, PRODUCER deposited value 0.64816
age=4238, PRODUCER napping for 2504 ms
age=4434, Consumer wants to consume
age=4436, Consumer fetched buffer who=PRODUCER value=0.64816 when=4236
age=4439, Consumer napping for 877 ms
age=5324, Consumer wants to consume
age()=6224, time to stop the threads and exit
                                            ... end of example run(s)  */
