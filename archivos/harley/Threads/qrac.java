import Utilities.*;

class QueueItem {

   private String value = "";
   private QueueItem nextItem = null;

   public QueueItem() {}

   public QueueItem(String value) { this.value = value; }

   public QueueItem(String value, QueueItem nextItem) {
      this.value = value;
      this.nextItem = nextItem;
   }

   public String toString() { return value; }

   public String valueGet() { return value; }

   public void valueSet(String value) { this.value = value; }

   public QueueItem nextItemGet() { return nextItem; }

   public void nextItemSet(QueueItem nextItem) { this.nextItem = nextItem; }
}

class QueueException extends Exception {}

class EmptyQueueException extends QueueException {}

class CorruptedQueueException extends QueueException {}

class Queue extends MyObject {

   private QueueItem head = null, tail = null;
   private int count = 0;

   public Queue() { System.out.println("Queue is alive"); }

   public String toString() {
      String result = "";
      QueueItem one = head;
      while (one != null) {
         result += " " + one;
         one = one.nextItemGet();
      }
      return result;
   }

   public void enQueue(String value, boolean doContextSwitch)
         throws CorruptedQueueException {
      QueueItem oldTail = null;
      QueueItem newOne = new QueueItem(value);
      if (tail == null) { // add to an empty queue
         if (head != null) throw new CorruptedQueueException();
         else {
            head = newOne; tail = newOne;
         }
      } else {            // add to end of queue
         oldTail = tail;
                                         if (doContextSwitch) nap(1500);
         tail = newOne;
         oldTail.nextItemSet(newOne);
      }
      count++;
      System.out.println("enQueue: there are now " + count
         + " nodes in the queue");
   }

   public String deQueue()
         throws EmptyQueueException, CorruptedQueueException {
      QueueItem oldOne = null;
      if (head == null && tail == null)
         throw new EmptyQueueException();
      else if (head != null && tail != null) {
         if (head == tail) { // dequeue from a singleton queue
            oldOne = head;
            head = null; tail = null;
         } else {            // dequeue from beginning of queue
            oldOne = head;
            head = oldOne.nextItemGet();
         }
         count--;
         System.out.println("deQueue: there are now " + count
            + " nodes in the queue");
      } else throw new CorruptedQueueException();
      return oldOne.valueGet();
   }
}

class Aenq extends MyObject implements Runnable {

   private Queue q = null;

   public Aenq(Queue q) {
      this.q = q;
   }
   public void run() {
      try {
         for (int i = 1; i <= 4; i++) {
            q.enQueue("item"+i, false);
            System.out.println("age()=" + age() + ", Aenq: q=\n" + q);
            nap(1000);
         }
      } catch (CorruptedQueueException e) {
         System.out.println(e);
      }
   }
}

class Benq extends MyObject implements Runnable {

   private Queue q = null;
   private boolean doContextSwitch = false;

   public Benq(Queue q, boolean doContextSwitch) {
      this.q = q;
      this.doContextSwitch = doContextSwitch;
   }

   public void run() {
      nap(1000);
      try {
         for (int i = 5; i <= 9; i++) {
            q.enQueue("item"+i, doContextSwitch && i==6);
            System.out.println("age()=" + age() + ", Benq: q=\n" + q);
            nap(1000);
         }
      } catch (CorruptedQueueException e) {
         System.out.println(e);
      }
   }
}

class QueueRace extends MyObject {

   private static void deQueueItemAndPrint(Queue q) {
      try {
         System.out.println("age()=" + age() + ", QueueRace: dequeue "
            + q.deQueue());
      } catch (QueueException e) {
         System.out.println(e);
      }
   }

   public static void main(String[] args) {
      Queue q = new Queue();
      boolean doContextSwitch = false;
      try {
         doContextSwitch = Boolean.valueOf(args[0]).booleanValue();
      } catch (ArrayIndexOutOfBoundsException e) {}
      System.out.println("QueueRace alive, doContextSwitch=" +
         doContextSwitch);
      new Thread(new Aenq(q)).start();
      new Thread(new Benq(q, doContextSwitch)).start();
      nap(2000);
      for (int i = 1; i <= 10; i++) {
         deQueueItemAndPrint(q);
         nap(1000*((i % 4) + 1));
      }
   }
}

/* ............... Example compile and run(s)

D:\>javac qrac.java

D:\>java QueueRace
Queue is alive
QueueRace alive, doContextSwitch=false
enQueue: there are now 1 nodes in the queue
age()=110, Aenq: q=
 item1
enQueue: there are now 2 nodes in the queue
age()=1150, Aenq: q=
 item1 item2
enQueue: there are now 3 nodes in the queue
age()=1150, Benq: q=
 item1 item2 item5
deQueue: there are now 2 nodes in the queue
age()=2090, QueueRace: dequeue item1
enQueue: there are now 3 nodes in the queue
age()=2140, Aenq: q=
 item2 item5 item3
enQueue: there are now 4 nodes in the queue
age()=2140, Benq: q=
 item2 item5 item3 item6
enQueue: there are now 5 nodes in the queue
age()=3130, Aenq: q=
 item2 item5 item3 item6 item4
enQueue: there are now 6 nodes in the queue
age()=3130, Benq: q=
 item2 item5 item3 item6 item4 item7
deQueue: there are now 5 nodes in the queue
age()=4120, QueueRace: dequeue item2
enQueue: there are now 6 nodes in the queue
age()=4170, Benq: q=
 item5 item3 item6 item4 item7 item8
enQueue: there are now 7 nodes in the queue
age()=5160, Benq: q=
 item5 item3 item6 item4 item7 item8 item9
deQueue: there are now 6 nodes in the queue
age()=7080, QueueRace: dequeue item5
deQueue: there are now 5 nodes in the queue
age()=11090, QueueRace: dequeue item3
deQueue: there are now 4 nodes in the queue
age()=12140, QueueRace: dequeue item6
deQueue: there are now 3 nodes in the queue
age()=14110, QueueRace: dequeue item4
deQueue: there are now 2 nodes in the queue
age()=17130, QueueRace: dequeue item7
deQueue: there are now 1 nodes in the queue
age()=21140, QueueRace: dequeue item8
deQueue: there are now 0 nodes in the queue
age()=22130, QueueRace: dequeue item9
EmptyQueueException

D:\>java QueueRace true
Queue is alive
QueueRace alive, doContextSwitch=true
enQueue: there are now 1 nodes in the queue
age()=50, Aenq: q=
 item1
enQueue: there are now 2 nodes in the queue
age()=1100, Aenq: q=
 item1 item2
enQueue: there are now 3 nodes in the queue
age()=1100, Benq: q=
 item1 item2 item5
deQueue: there are now 2 nodes in the queue
age()=2030, QueueRace: dequeue item1
enQueue: there are now 3 nodes in the queue
age()=2090, Aenq: q=
 item2 item5 item3
enQueue: there are now 4 nodes in the queue
age()=3080, Aenq: q=
 item2 item5 item3 item4
enQueue: there are now 5 nodes in the queue
age()=3570, Benq: q=
 item2 item5 item3 item6
deQueue: there are now 4 nodes in the queue
age()=4060, QueueRace: dequeue item2
enQueue: there are now 5 nodes in the queue
age()=4610, Benq: q=
 item5 item3 item6 item7
enQueue: there are now 6 nodes in the queue
age()=5600, Benq: q=
 item5 item3 item6 item7 item8
enQueue: there are now 7 nodes in the queue
age()=6590, Benq: q=
 item5 item3 item6 item7 item8 item9
deQueue: there are now 6 nodes in the queue
age()=7080, QueueRace: dequeue item5
deQueue: there are now 5 nodes in the queue
age()=11090, QueueRace: dequeue item3
deQueue: there are now 4 nodes in the queue
age()=12080, QueueRace: dequeue item6
deQueue: there are now 3 nodes in the queue
age()=14060, QueueRace: dequeue item7
deQueue: there are now 2 nodes in the queue
age()=17080, QueueRace: dequeue item8
deQueue: there are now 1 nodes in the queue
age()=21090, QueueRace: dequeue item9
EmptyQueueException
EmptyQueueException
                                            ... end of example run(s)  */
