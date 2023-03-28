import java.util.Vector;

public interface RendezvousCondition {

/*
 * The information available to the checkCondition method is:
 *   the particular message being evaluated,
 *     blockedMessages.elementAt(messageNum);
 *   the queue of blocked messages itself, blockedMessages; and
 *   the number of blocked servers, numBlockedServers.
 * This is the state of the ConditionalRendezvous object.  The particular
 * message can be checked to see if it meets the condition and this test
 * may involving counting how many blocked messages meet some other criterion
 * and/or the number of blocked servers.
 *
 * We are depending on the programmer not to mess with the blockedMessages
 * Vector.  The ConditionalRendezvous object is graciously making it
 * available, so do not abuse!
 */

   public abstract boolean checkCondition
      (int messageNum, Vector blockedMessages, int numBlockedServers);
}
