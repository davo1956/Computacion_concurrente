package Synchronization;

import java.net.*;
import java.io.*;
import Utilities.*;

public class EstablishRendezvous extends MyObject {

/*
 *   Objects of this class are used as places to establish a rendezvous.
 * Constructors are available for the local case (client and server in
 * the same JVM) and for client and server on different JVMs (using a
 * socket).  A common object of this class is used for the local case,
 * in which the client and server call clientToServer and serverToClient,
 * respectively, to get an ExtendedRendezvous object containing the
 * communication channel (SyncMessagePassing for local).  For the remote
 * case, the client and server create their own objects of this class.
 * Then they call call clientToServer and serverToClient to get their
 * ExtendedRendezvous objects to conduct transactions (serialized objects
 * through a socket for between JVMs).  The case of sending raw data types
 * through a pipe (same JVM) or a socket (different JVMs) is not handled
 * and is an exercise for the reader.
 *   In the local case, one object of this class can be shared by multiple
 * client threads and server threads.  In the remote case, one server-side
 * object of this class can be shared by multiple servers and one client-
 * side object of this class shared by multiple clients.
 */

   private MessagePassing in = null;
   private String machineName = null;
   private int portNum = -1;           // stays -1 in local case as a flag
   private ServerSocket serverSocket = null;

   // local, for both clients and servers
   public EstablishRendezvous() {
      super("EstablishRendezvous");
      in = new SyncMessagePassing();
   }

   // server side, contains ServerSocket
   public EstablishRendezvous(int portNum) throws MessagePassingException {
      super("EstablishRendezvous: portNum=" + portNum);
      this.portNum = portNum;
      try {
         serverSocket = new ServerSocket(portNum, 50);
      } catch (IOException e) {
         System.err.println("EstablishRendezvous: cannot ServerSocket on "
            + portNum + " port, " + e);
         throw new MessagePassingException();
      }
   }

   // client side, save machine name and port
   public EstablishRendezvous(String machineName, int portNum) {
      super("EstablishRendezvous: machineName=" + machineName
         + " portNum=" + portNum);
      if (machineName == null || portNum < 0) {
         System.err.println("EstablishRendezvous: bad machine name or port "
            + machineName + ", " + portNum);
         throw new NullPointerException();
      }
      this.machineName = machineName;
      this.portNum = portNum;
   }

// Server calls this method to get the ExtendedRendezvous object to use
// with the client.
   public Rendezvous serverToClient() throws MessagePassingException {
      if (portNum < 0) return (Rendezvous) receive(in);  // local case
      Socket socket = null;
      try {                                              // remote case
         socket = serverSocket.accept();
      } catch (IOException e) {
         throw new MessagePassingException();
      }
      Rendezvous r = new ExtendedRendezvous(socket);
      return r;
   }

// Client calls this method to get the ExtendedRendezvous object
// to use with the server.
   public Rendezvous clientToServer() throws MessagePassingException {
      if (portNum < 0) {                                // local case
         Rendezvous r = new ExtendedRendezvous();
         send(in, r);
         return r;
      } else {                                          // remote case
         Socket socket = null;
         try {
            socket = new Socket(machineName, portNum);
         } catch (IOException e) {
            throw new MessagePassingException();
         }
         Rendezvous r = new ExtendedRendezvous(socket);
         return r;
      }
   }

   public void close() {
      try {
         if (serverSocket != null) serverSocket.close();
      } catch (IOException e) {
         System.err.println(getName() + ", close: " + e);
      }
   }

   public String toString() { return getName(); }
}
