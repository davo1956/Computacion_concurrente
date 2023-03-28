package Synchronization;

import Utilities.*;

public abstract class MessagePassingRoot extends MyObject
      implements MessagePassing {

   public MessagePassingRoot() {super();}
   public MessagePassingRoot(String name) {super(name);}

   public abstract String toString();

   public abstract void send(Object m)
      throws NotImplementedMethodException;

   public void send(int m) {send(this, new Integer(m));}

   public void send(double m) {send(this, new Double(m));}

   public abstract Object receive()
      throws NotImplementedMethodException;

   public int receiveInt() throws ClassCastException {
      return ((Integer) receive(this)).intValue();
   }

   public double receiveDouble() throws ClassCastException {
      return ((Double) receive(this)).doubleValue();
   }

   public void close() throws NotImplementedMethodException {
      throw new NotImplementedMethodException();
   }
}
