public final class MessagePassingSendOnly extends MyObject
      implements MessagePassing {

   private MessagePassing mp = null;

   public MessagePassingSendOnly(MessagePassing mp) {
      super("MessagePassingSendOnly");
      this.mp = mp;
   }

   public void send(int m) { send(mp, m); }

   public void send(double m) { send(mp, m); }

   public void send(Object m) { send(mp, m); }

   public int receiveInt()
      {throw new NotImplementedMethodException();}

   public double receiveDouble()
      {throw new NotImplementedMethodException();}

   public Object receive()
      {throw new NotImplementedMethodException();}

   public void close() { mp.close(); }
}
