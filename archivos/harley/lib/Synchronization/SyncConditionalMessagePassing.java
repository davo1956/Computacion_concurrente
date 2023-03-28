package Synchronization;

import java.util.Vector;
import Synchronization.*;
import Utilities.*;

public class SyncConditionalMessagePassing extends MyObject
      implements ConditionalMessagePassing, Runnable {

   private Thread me = null;
   private Vector blockedMessages = null;
   private Vector blockedSenders = null;
   private Vector blockedConditions = null;
   private Vector blockedReceivers = null;
   private boolean debug = false;

   public SyncConditionalMessagePassing(boolean debug) {
      super("SyncConditionalMessagePassing: debug=" + debug);
      this.debug = debug;
      blockedMessages = new Vector();
      blockedSenders = new Vector();
      blockedConditions = new Vector();
      blockedReceivers = new Vector();
      me = new Thread(this);
      me.setDaemon(true);
      me.start();
   }

   public SyncConditionalMessagePassing() { this(false); }

   private boolean matchedMessageWithReceiver() {
      int numMessages = blockedMessages.size();
      int numReceivers = blockedReceivers.size();
      if (debug) System.out.println("SCMP: matching " + numMessages
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
               BinarySemaphore bs =
                  (BinarySemaphore) blockedSenders.elementAt(j);
               blockedSenders.removeElementAt(j);
               mp.send(m);
               V(bs);
               if (debug)
                  System.out.println("SCMP: matched " + j + " with " + i);
               return true;
            }
         }
      }
      if (debug) System.out.println("SCMP: no matches found");
      return false;
   }

   public void run() {
      synchronized (me) {
         while (true) {
            while (matchedMessageWithReceiver()) ;
            if (debug) System.out.println("SCMP: waiting");
            try { me.wait(); } catch (InterruptedException e) {}
            if (debug) System.out.println("SCMP: done waiting");
         }
      }
   }

   public void send(Object message) {
      if (message == null) {
         System.err.println("null message passed to send()");
         throw new NullPointerException();
      }
      BinarySemaphore bs = new BinarySemaphore(0);
      synchronized (me) {
         blockedMessages.addElement(message);
         blockedSenders.addElement(bs);
         me.notify();
      }
      if (debug) System.out.println("SCMP: send notify");
      P(bs);
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
      if (debug) System.out.println("SCMP: receive notify");
      return mp.receive();
   }

   public void close() {
      if (me != null) me.stop();
   }
}
