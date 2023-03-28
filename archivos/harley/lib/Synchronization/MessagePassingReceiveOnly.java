package Synchronization;

import Utilities.*;

public final class MessagePassingReceiveOnly extends MyObject
      implements MessagePassing {

   private MessagePassing mp = null;

   public MessagePassingReceiveOnly(MessagePassing mp) {
      super("MessagePassingReceiveOnly");
      this.mp = mp;
   }

   public void send(int m)
      {throw new NotImplementedMethodException();}

   public void send(double m)
      {throw new NotImplementedMethodException();}

   public void send(Object m)
      {throw new NotImplementedMethodException();}

   public int receiveInt() { return receiveInt(mp); }

   public double receiveDouble() { return receiveDouble(mp); }

   public Object receive() { return receive(mp); }

   public void close() { mp.close(); }
}
