package Synchronization;

import java.net.*;
import java.io.*;

// like a bounded buffer with multiple producers and consumers
public final class PipedMessagePassing extends MessagePassingRoot {

   private PipedOutputStream outPipe = null;
   private PipedInputStream inPipe = null;
   private DataOutputStream outData = null;
   private DataInputStream inData = null;
   private Socket socket = null;
   private final Object sending = new Object();
   private final Object receiving = new Object();

   // construct a pipe through which the base data types int and double
   // can be passed in raw or binary format within the same JVM
   public PipedMessagePassing() {
      super("PipedMessagePassing");
      outPipe = new PipedOutputStream();
      try {
         inPipe = new PipedInputStream(outPipe);
      } catch (IOException e) {
         System.err.println("PipedMessagePassing:" + e);
         throw new MessagePassingException();
      }
      outData = new DataOutputStream(outPipe);
      inData = new DataInputStream(inPipe);
   }

   // construct a socket through which the base data types int and double
   // can be passed in raw or binary format between JVMs
   public PipedMessagePassing(Socket socket) {
      super("PipedMessagePassing");
      if (socket == null) {
         System.err.println("null socket passed to PipedMessagePassing");
         throw new NullPointerException();
      }
      this.socket = socket;
      try {
         outData = new DataOutputStream(socket.getOutputStream());
         inData = new DataInputStream(socket.getInputStream());
      } catch (IOException e) {
         System.err.println("PipedMessagePassing:" + e);
         throw new MessagePassingException();
      }
   }

   public String toString() { return "PipedMessagePassing"; }

   public final void send(int m) {
      synchronized (sending) {
         try {
            outData.writeInt(m);
            outData.flush();     // wake up waiting readers before the pipe
            if (outPipe != null)
               outPipe.flush();  // fills instead of not until the pipe fills
         } catch (IOException e) {
            System.err.println("PipedMessagePassing.send:" + e);
            throw new MessagePassingException();
         }
      }
   }

   public final void send(double m) {
      synchronized (sending) {
         try {
            outData.writeDouble(m);
            outData.flush();     // wake up waiting readers before the pipe
            if (outPipe != null)
               outPipe.flush();  // fills instead of not until the pipe fills
         } catch (IOException e) {
            System.err.println("PipedMessagePassing.send:" + e);
            throw new MessagePassingException();
         }
      }
   }

   public final void send(Object m) throws NotImplementedMethodException {
      throw new NotImplementedMethodException();
   }

   public final Object receive() throws NotImplementedMethodException {
      throw new NotImplementedMethodException();
      // return null;  // compiler says "Statement not reached."
   }

   public final int receiveInt() {
      int value = 0;
      synchronized (receiving) {
         try {
            value = inData.readInt();
         } catch (IOException e) {
            System.err.println("PipedMessagePassing.receive:" + e);
            throw new MessagePassingException();
         }
      }
      return value;
   }

   public final double receiveDouble() {
      double value = 0.0;
      synchronized (receiving) {
         try {
            value = inData.readDouble();
         } catch (IOException e) {
            System.err.println("PipedMessagePassing.receive:" + e);
            throw new MessagePassingException();
         }
      }
      return value;
   }

   public void close() {
      try {
         if (inData != null) inData.close();
         if (outData != null) outData.close();
         if (inPipe != null) inPipe.close();
         if (outPipe != null) outPipe.close();
         if (socket != null) socket.close();
      } catch (IOException e) {
         System.err.println(getName() + ", close: " + e);
      }
   }
}
