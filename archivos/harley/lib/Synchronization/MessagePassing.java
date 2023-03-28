package Synchronization;

public interface MessagePassing {

   public abstract void send(Object m)
      throws NotImplementedMethodException;

   public abstract void send(int m)
      throws NotImplementedMethodException ;

   public abstract void send(double m)
      throws NotImplementedMethodException;

   public abstract Object receive()
      throws NotImplementedMethodException;

   public abstract int receiveInt() throws ClassCastException,
      NotImplementedMethodException;

   public abstract double receiveDouble() throws ClassCastException,
      NotImplementedMethodException;

   public abstract void close()
      throws NotImplementedMethodException;
}
