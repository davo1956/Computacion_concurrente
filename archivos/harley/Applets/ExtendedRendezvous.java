import java.net.*;

/*
 *   Extended Rendezvous: a two-way flow of information, much like a
 * remote procedute call made by a client thread to a server thread.
 * A simple rendezvous is synchronous message passing, a one-way flow
 * of information while sender and receiver are blocked.  In the extended
 * kind the client thread sends an object to the server that represents a
 * request and blocks waiting for the reply; the server blocks waiting
 * for a request.  The server gets the request, computes the reply,
 * and sends it to the client, unblocking it.  The server may spawn
 * off a thread to handle the request while the server gets additional
 * requests.  Another paradigm represented by this class is master/worker.
 * The master waits for requests from the worker threads that they are
 * able to do another piece of work.  The master sends a work command to
 * the worker, who sends the result back later in another rendezvous (using
 * the same ExtendedRendezvous object or calling the EstablishRendezvous
 * object again).  In the same JVM, synchronous message passing is used
 * because there is no need for buffered sends.  Between JVMs that might
 * be on different physical machines, sockets are used.  The other
 * possibilities (sending raw data types through a pipe or socket) are
 * exercises for the reader.
 *   A particular object of this class is designed to be used by only one
 * client thread since method clientMakeRequestAwaitReply is not
 * synchronized (if client threads did try to share, the replies coming
 * back would not match the requests unless the method were synchronized).
 * Even if the method were synchronized, the client transactions would then
 * be allowed only one a time with the server, i.e., no concurrency.  So
 * each client should have its one object of this class.
 *   On the other hand, a particular object of this class can be shared by
 * by multiple servers, but there is not much point in that since the
 * one client on the other end could keep only one server at a time busy.
 */

public class ExtendedRendezvous extends MyObject implements Rendezvous {
// designed to be used by a single client transacting with a server
   private MessagePassing in = null;
   private MessagePassing out = null;

   public ExtendedRendezvous() {
      super("ExtendedRendezvous");
/*
 * We used synchronous message passing rather than asynchronous because
 * buffering is unnecessary and would waste space; requester blocks on
 * the send in synchronous case and on the receive in asynchronous case.
 */
      this.in = new SyncMessagePassing();
      this.out = new SyncMessagePassing();
   }

   public ExtendedRendezvous(Socket socket) {
      super("ExtendedRendezvous");
      this.in = new ObjPipedMessagePassing(socket);
      this.out = this.in;
   }

/*
 * We do not need synchronized methods here because synchronization is
 * handled inside the send and receive methods.  This method should be
 * synchronized if there are multiple client threads sharing this object.
 */
   public Object clientMakeRequestAwaitReply(Object m) {
      send(in, m);
      return receive(out);
   }

/*
 * A deadlock possibility exists: if the server makes another call to
 * serverGetRequest() before calling serverMakeReply() then this Extended
 * Rendezvous object is deadlocked (assuming just one client is using this
 * object, the intended situation) in the sense that the client is blocked
 * on receive(out) and the server is blocked on receive(in).  This needs
 * to be fixed.
 */
   public Object serverGetRequest() {
      return receive(in);
   }

   public void serverMakeReply(Object m) {
      send(out, m);
   }

   public void close() {
      try {
         in.close();
         out.close();
      } catch (NotImplementedMethodException e) {}
   }

   public String toString() { return getName(); }
}
