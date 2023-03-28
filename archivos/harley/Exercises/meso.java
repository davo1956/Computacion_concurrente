import Utilities.*;
import Synchronization.*;

class Worker extends MyObject implements Runnable {

   private int id = -1;
   private MessagePassing channel = null;

   public Worker(int id, MessagePassing channel) {
      this.id = id;
      this.channel = channel;
      new Thread(this).start();
   }

   public void run() {
      int item = id;
      send(channel, item);
   }
}

class MergeSort extends MyObject {

   public static void main(String[] args) {
      int numWorkers = 2;
      AsyncMessagePassing[] channel =
         new AsyncMessagePassing[numWorkers];
      for (int i = 0; i < numWorkers; i++) {
         channel[i] = new AsyncMessagePassing();
         new Worker(i, channel[i]);
      }
      for (int i = 0; i < numWorkers; i++) {
         int value = receiveInt(channel[i]);
         System.out.println("MergeSort received value " + value
            + " on channel " + i);
      }
   }
}

/* ............... Example compile and run(s)

D:\>javac meso.java

D:\>java MergeSort
MergeSort received value 0 on channel 0
MergeSort received value 1 on channel 1
                                ... end of example run(s)  */
