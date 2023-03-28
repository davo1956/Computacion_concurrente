public interface Rendezvous {

   public abstract Object clientMakeRequestAwaitReply(Object m);

   public abstract Object serverGetRequest();

   public abstract void serverMakeReply(Object m);

   public abstract String toString();

   public abstract void close();
}
