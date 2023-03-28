import java.util.Vector;

public class AsyncConditionalMessagePassing extends MyObject
      implements ConditionalMessagePassing, Runnable {

   private Thread me = null;
   private Vector blockedMessages = null;
   private Vector blockedConditions = null;
   private Vector blockedReceivers = null;
   private boolean debug = false;

   public AsyncConditionalMessagePassing(boolean debug) {
      super("AsyncConditionalMessagePassing: debug=" + debug);
      this.debug = debug;
      blockedMessages = new Vector();
      blockedConditions = new Vector();
      blockedReceivers = new Vector();
      me = new Thread(this);
      me.setDaemon(true);
      me.start();
   }

   public AsyncConditionalMessagePassing() { this(false); }

   private boolean matchedMessageWithReceiver() {
      int numMessages = blockedMessages.size();
      int numReceivers = blockedReceivers.size();
      if (debug) System.out.println("ACMP: matching " + numMessages
         + " messages against " + numReceivers + " receivers");
      if (numMessages == 0 || numReceivers == 0) return false;
      for (int i = 0; i < numReceivers; i++) {
         for (int j = 0; j < numMessages; j++) {
            Object m = blockedMessages.elementAt(j);
            Condition c = (Condition) blockedConditions.elementAt(i);
            if (c.checkCondition(m)) {
               blockedMessages.removeElementAt(j);
               blockedConditions.removeElementAt(i);
               SyncMessagePassing mp =
                  (SyncMessagePassing) blockedReceivers.elementAt(i);
               blockedReceivers.removeElementAt(i);
               mp.send(m);
               if (debug)
                  System.out.println("ACMP: matched " + j + " with " + i);
               return true;
            }
         }
      }
      if (debug) System.out.println("ACMP: no matches found");
      return false;
   }

   public void run() {
      synchronized (me) {
         while (true) {
            while (matchedMessageWithReceiver()) ;
            if (debug) System.out.println("ACMP: waiting");
            try { me.wait(); } catch (InterruptedException e) {}
            if (debug) System.out.println("ACMP: done waiting");
         }
      }
   }

   public void send(Object message) {
      if (message == null) {
         System.err.println("null message passed to send()");
         throw new NullPointerException();
      }
      synchronized (me) {
         blockedMessages.addElement(message);
         me.notify();
      }
      if (debug) System.out.println("ACMP: send notify");
   }

   public Object receive(Condition condition) {
      if (condition == null) {
         System.err.println("null condition passed to receive()");
         throw new NullPointerException();
      }
      SyncMessagePassing mp = new SyncMessagePassing();
      synchronized (me) {
         blockedConditions.addElement(condition);
         blockedReceivers.addElement(mp);
         me.notify();
      }
      if (debug) System.out.println("ACMP: receive notify");
      return mp.receive();
   }

   public void close() {
      if (me != null) me.stop();
   }
}
