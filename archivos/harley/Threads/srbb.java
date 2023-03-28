class BufferItem {
   public volatile double value = 0;          // multiple threads access
   public volatile boolean occupied = false;  // so make these `volatile'
   public volatile Thread thread = null;
}

class BoundedBuffer {  // designed for a single producer thread and
                       // a single consumer thread
   private int numSlots = 0;
   private BufferItem[] buffer = null;
   private int putIn = 0, takeOut = 0;

   public BoundedBuffer(int numSlots) {
      if (numSlots <= 0) throw new IllegalArgumentException("numSlots<=0");
      this.numSlots = numSlots;
      buffer = new BufferItem[numSlots];
      for (int i = 0; i < numSlots; i++) buffer[i] = new BufferItem();
   }

   public void deposit(double value) {
      if (buffer[putIn].occupied) {
         Thread producer = Thread.currentThread();
         buffer[putIn].thread = producer;
//                      producer.yield(); // force "bad" context switch
         producer.suspend();
         buffer[putIn].thread = null;
      }
      buffer[putIn].value = value;
      buffer[putIn].occupied = true;
      Thread consumer = buffer[putIn].thread;
      putIn = (putIn + 1) % numSlots;
      if (consumer != null) consumer.resume(); // a consumer is waiting
   }

   public double fetch() {
      double value;
      if (!buffer[takeOut].occupied) {
         Thread consumer = Thread.currentThread();
         buffer[takeOut].thread = consumer;
//                      consumer.yield(); // force "bad" context switch
         consumer.suspend();
         buffer[takeOut].thread = null;
      }
      value = buffer[takeOut].value;
      buffer[takeOut].occupied = false;
      Thread producer = buffer[takeOut].thread;
      takeOut = (takeOut + 1) % numSlots;
      if (producer != null) producer.resume(); // a producer is waiting
      return value;
   }
}

/* ............... Example compile and run(s)

D:\>javac srbb.java bbpc.java

D:\>java ProducerConsumer -s6 -p2 -c2 -R10
ProducerConsumer: numSlots=6, pNap=2, cNap=2, runTime=10
All threads started
age=60, PRODUCER napping for 643 ms
age=60, Consumer napping for 1851 ms
age=720, PRODUCER produced item 0.123381
PRODUCER deposited item 0.123381
age=720, PRODUCER napping for 663 ms
age=1380, PRODUCER produced item 0.396578
PRODUCER deposited item 0.396578
age=1380, PRODUCER napping for 1900 ms
age=1930, Consumer wants to consume
Consumer fetched item 0.123381
age=1930, Consumer napping for 140 ms
age=2090, Consumer wants to consume
Consumer fetched item 0.396578
age=2090, Consumer napping for 1623 ms
age=3300, PRODUCER produced item 0.660364
PRODUCER deposited item 0.660364
age=3300, PRODUCER napping for 1541 ms
age=3680, Consumer wants to consume
Consumer fetched item 0.660364
age=3680, Consumer napping for 1119 ms
age=4840, Consumer wants to consume
age=4840, PRODUCER produced item 0.886704
PRODUCER deposited item 0.886704
age=4840, PRODUCER napping for 1616 ms
Consumer fetched item 0.886704
age=4840, Consumer napping for 909 ms
age=5770, Consumer wants to consume
age=6430, PRODUCER produced item 0.318411
PRODUCER deposited item 0.318411
age=6430, PRODUCER napping for 1507 ms
Consumer fetched item 0.318411
age=6430, Consumer napping for 969 ms
age=7420, Consumer wants to consume
age=7970, PRODUCER produced item 0.552109
PRODUCER deposited item 0.552109
age=7970, PRODUCER napping for 812 ms
Consumer fetched item 0.552109
age=7970, Consumer napping for 817 ms
age=8790, PRODUCER produced item 0.0881424
PRODUCER deposited item 0.0881424
age=8790, PRODUCER napping for 119 ms
age=8790, Consumer wants to consume
Consumer fetched item 0.0881424
age=8790, Consumer napping for 649 ms
age=8900, PRODUCER produced item 0.502511
PRODUCER deposited item 0.502511
age=8900, PRODUCER napping for 712 ms
age=9450, Consumer wants to consume
Consumer fetched item 0.502511
age=9450, Consumer napping for 838 ms
age=9620, PRODUCER produced item 0.0185393
PRODUCER deposited item 0.0185393
age=9620, PRODUCER napping for 575 ms
age()=10060, time to stop the threads and exit
                                            ... end of example run(s)  */
