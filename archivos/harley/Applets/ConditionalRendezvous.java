import java.net.*;
import java.io.*;
import java.util.Vector;

public class ConditionalRendezvous extends MyObject implements Runnable {

   private Thread me = null;
   private Vector blockedMessages = null;
   private Vector blockedClients = null;
   private Vector blockedConditions = null;
   private Vector blockedServers = null;
   private boolean debug = false;

/*
 * Objects of this class are used as places to establish a conditional
 * rendezvous.  Several conditional rendezvous can be in progress
 * simultaneously by one or more servers, that is, they can be nested.
 */

   public ConditionalRendezvous(boolean debug) {
      super("ConditionalRendezvous: debug=" + debug);
      this.debug = debug;
      blockedMessages = new Vector();
      blockedClients = new Vector();
      blockedConditions = new Vector();
      blockedServers = new Vector();
      me = new Thread(this);
      me.setDaemon(true);
      me.start();
   }

   public ConditionalRendezvous() { this(false); }

   private boolean matchedMessageWithReceiver() {
      int numMessages = blockedMessages.size();
      int numServers = blockedServers.size();
      if (debug) System.out.println("CondRend: matching " + numMessages
         + " messages against " + numServers + " receivers");
      if (numMessages == 0 || numServers == 0) return false;
      for (int i = 0; i < numServers; i++) {
         for (int j = 0; j < numMessages; j++) {
            RendezvousCondition c = (RendezvousCondition)
                  blockedConditions.elementAt(i);
/*
 * We are running security and protection risks making the blockedMessages
 * Vector available to the outside. Caveat emptor!
 */
            if (c.checkCondition(j, blockedMessages, numServers)) {
               blockedMessages.removeElementAt(j);
               blockedConditions.removeElementAt(i);
               SyncMessagePassing mp =
                  (SyncMessagePassing) blockedServers.elementAt(i);
               blockedServers.removeElementAt(i);
               Rendezvous r = (Rendezvous) blockedClients.elementAt(j);
               blockedClients.removeElementAt(j);
               mp.send(r);
               if (debug)
                  System.out.println("CondRend: matched " + j + " with " + i);
               return true;
            }
         }
      }
      if (debug) System.out.println("CondRend: no matches found");
      return false;
   }

   public void run() {
      synchronized (me) {
         while (true) {
            while (matchedMessageWithReceiver()) ;
            if (debug) System.out.println("CondRend: waiting");
            try { me.wait(); } catch (InterruptedException e) {}
            if (debug) System.out.println("CondRend: done waiting");
         }
      }
   }

// Server calls this method to get the ExtendedRendezvous object to use
// with the client.
   public Rendezvous serverGetClient(RendezvousCondition condition) {
      if (condition == null) {
         System.err.println("null condition passed to serverGetClient()");
         throw new NullPointerException();
      }
      SyncMessagePassing mp = new SyncMessagePassing();
      synchronized (me) {
         blockedConditions.addElement(condition);
         blockedServers.addElement(mp);
         me.notify();
      }
      if (debug) System.out.println("CondRend: receive notify");
      return (Rendezvous) mp.receive();
   }

// Client calls this method to use with the server.
   public Object clientTransactServer(Object message) {
      if (message == null) {
         System.err.println("null message passed to clientTransactServer()");
         throw new NullPointerException();
      }
      Rendezvous r = new ExtendedRendezvous();
      synchronized (me) {
         blockedMessages.addElement(message);
         blockedClients.addElement(r);
         me.notify();
      }
      if (debug) System.out.println("CondRend: send notify");
      return r.clientMakeRequestAwaitReply(message);
   }

   public void close() {
      if (me != null) me.stop();
   }

   public String toString() { return getName(); }
}
