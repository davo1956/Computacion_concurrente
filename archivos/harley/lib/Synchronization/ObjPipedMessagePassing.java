package Synchronization;

import java.net.*;
import java.io.*;

// like a bounded buffer with multiple producers and consumers
public final class ObjPipedMessagePassing extends MessagePassingRoot {

   private PipedOutputStream outPipe = null;
   private PipedInputStream inPipe = null;
   private ObjectOutputStream outObj = null;
   private ObjectInputStream inObj = null;
   private final Object sending = new Object();
   private final Object receiving = new Object();
   private Socket socket = null;

   // construct a pipe through which objects can be serialized and
   // deserialized within the same JVM
   public ObjPipedMessagePassing() {
      super("ObjPipedMessagePassing");
      outPipe = new PipedOutputStream();
      try {
         inPipe = new PipedInputStream(outPipe);
      } catch (IOException e) {
         System.err.println("ObjPipedMessagePassing:" + e);
         throw new MessagePassingException();
      }
      try {
         outObj = new ObjectOutputStream(outPipe);
         inObj = new ObjectInputStream(inPipe);
      } catch (IOException e) {
         System.err.println("ObjPipedMessagePassing:" + e);
         throw new MessagePassingException();
      }
   }

   // construct a socket through which objects can be serialized and
   // deserialized between JVMs
   public ObjPipedMessagePassing(Socket socket) {
      super("ObjPipedMessagePassing");
      if (socket == null) {
         System.err.println("null socket passed to ObjPipedMessagePassing");
         throw new NullPointerException();
      }
      this.socket = socket;
      try {
         outObj = new ObjectOutputStream(socket.getOutputStream());
         inObj = new ObjectInputStream(socket.getInputStream());
      } catch (IOException e) {
         System.err.println("ObjPipedMessagePassing:" + e);
         throw new MessagePassingException();
      }
   }

   public String toString() { return "ObjPipedMessagePassing"; }

   public final void send(Object m) {
      synchronized (sending) {
         try {
            outObj.writeObject(m);
            outObj.flush();
            if (outPipe != null) // wake up waiting readers before the pipe
               outPipe.flush();  // fills instead of not until the pipe fills
         } catch (IOException e) {
            System.err.println("ObjPipedMessagePassing.send:" + e);
            throw new MessagePassingException();
         }
      }
   }

   public final Object receive() {
      Object o = null;
      synchronized (receiving) {
         try {
            o = inObj.readObject();
         } catch (Exception e) { // half a dozen are possible
            System.err.println("ObjPipedMessagePassing.receive:" + e);
            throw new MessagePassingException();
         }
      }
      return o;
   }

   public void close() {
      try {
         if (inObj != null) inObj.close();
         if (outObj != null) outObj.close();
         if (inPipe != null) inPipe.close();
         if (outPipe != null) outPipe.close();
         if (socket != null) socket.close();
      } catch (IOException e) {
         System.err.println(getName() + ", close: " + e);
      }
   }
}
