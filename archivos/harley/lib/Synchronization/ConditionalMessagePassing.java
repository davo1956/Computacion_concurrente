package Synchronization;

public interface ConditionalMessagePassing {

   public abstract void send(Object message);

   public abstract Object receive(Condition condition);

   public abstract void close();
}
