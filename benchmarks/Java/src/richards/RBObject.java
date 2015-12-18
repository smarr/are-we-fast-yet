package richards;


abstract class RBObject {
  public Packet append(final Packet packet, final Packet queueHead) {
    packet.setLink(NO_WORK);
    if (NO_WORK == queueHead) {
      return packet;
    }

    Packet mouse = queueHead;
    Packet link;
    while (NO_WORK != (link = mouse.getLink())) {
      mouse = link;
    }
    mouse.setLink(packet);
    return queueHead;
  }

  public static final int IDLER     = 0;
  public static final int WORKER    = 1;
  public static final int HANDLER_A = 2;
  public static final int HANDLER_B = 3;
  public static final int DEVICE_A  = 4;
  public static final int DEVICE_B  = 5;
  public static final int NUM_TYPES = 6;

  public static final int DEVICE_PACKET_KIND = 0;
  public static final int WORK_PACKET_KIND   = 1;

  public static final Packet NO_WORK = null;
  public static final TaskControlBlock NO_TASK = null;
}
