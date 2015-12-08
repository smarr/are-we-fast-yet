package richards;


public class TaskControlBlock extends TaskState {
  private TaskControlBlock link;
  private int identity;
  private int priority;
  private Packet input;
  private ProcessFunction function;
  private RBObject handle;

  public int getIdentity() { return identity; }
  public TaskControlBlock getLink()  { return link; }
  public int getPriority() { return priority; }

  public void initialize(final TaskControlBlock aLink, final int anIdentity,
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

  public TaskControlBlock addInputAndCheckPriority(final Packet packet,
      final TaskControlBlock oldTask) {
    if (RBObject.noWork() == input) {
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
      if (RBObject.noWork() == input) {
        running();
      } else {
        packetPending();
      }
    } else {
      message = RBObject.noWork();
    }
    return function.apply(message, handle);
  }

  public static TaskControlBlock create(final TaskControlBlock link, final int identity,
      final int priority, final Packet initialWorkQueue,
      final TaskState initialState, final ProcessFunction aBlock,
      final RBObject privateData) {
    TaskControlBlock t = new TaskControlBlock();
    t.initialize(link, identity, priority, initialWorkQueue,
        initialState, aBlock, privateData);
    return t;
  }
}
