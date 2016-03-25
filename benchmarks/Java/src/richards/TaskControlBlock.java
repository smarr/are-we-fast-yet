/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * Richards.
 * 
 * It is modified based on the SOM version and to use Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package richards;


final class TaskControlBlock extends TaskState {
  private final TaskControlBlock link;
  private final int identity;
  private final int priority;
  private Packet input;
  private final ProcessFunction function;
  private final RBObject handle;

  TaskControlBlock(final TaskControlBlock aLink, final int anIdentity,
      final int aPriority, final Packet anInitialWorkQueue,
      final TaskState anInitialState, final ProcessFunction aBlock,
      final RBObject aPrivateData) {
    link = aLink;
    identity = anIdentity;
    priority = aPriority;
    input = anInitialWorkQueue;
    setPacketPending(anInitialState.isPacketPending());
    setTaskWaiting(anInitialState.isTaskWaiting());
    setTaskHolding(anInitialState.isTaskHolding());
    function = aBlock;
    handle = aPrivateData;
  }

  public int getIdentity() { return identity; }
  public TaskControlBlock getLink()  { return link; }
  public int getPriority() { return priority; }

  public TaskControlBlock addInputAndCheckPriority(final Packet packet,
      final TaskControlBlock oldTask) {
    if (NO_WORK == input) {
      input = packet;
      setPacketPending(true);
      if (priority > oldTask.getPriority()) { return this; }
    } else {
      input = append(packet, input);
    }
    return oldTask;
  }

  public TaskControlBlock runTask() {
    Packet message;
    if (isWaitingWithPacket()) {
      message = input;
      input = message.getLink();
      if (NO_WORK == input) {
        running();
      } else {
        packetPending();
      }
    } else {
      message = NO_WORK;
    }
    return function.apply(message, handle);
  }
}
