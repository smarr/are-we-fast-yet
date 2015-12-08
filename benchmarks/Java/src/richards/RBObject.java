package som.richards;


public class RBObject {
  public Packet append(final Packet packet, final Packet queueHead) {
    packet.setLink(RBObject.noWork());
    if (RBObject.noWork() == queueHead) {
      return packet;
    }

    Packet mouse = queueHead;
    Packet link;
    while (RBObject.noWork() != (link = mouse.getLink())) {
      mouse = link;
    }
    mouse.setLink(packet);
    return queueHead;
  }

  private static int    DeviceA;
  private static int    DeviceB;
  private static int    DevicePacketKind;
  private static int    HandlerA;
  private static int    HandlerB;
  private static int    Idler;
  private static TaskControlBlock NoTask;
  private static Packet NoWork;
  private static int    Worker;
  private static int    WorkPacketKind;

  public static TaskControlBlock noTask() { return NoTask; }
  public static int    idler()  { return Idler;  }
  public static Packet noWork() { return NoWork; }
  public static int    worker() { return Worker; }
  public static int    workPacketKind() { return WorkPacketKind; }
  public static int    handlerA() { return HandlerA; }
  public static int    handlerB() { return HandlerB; }
  public static int    deviceA()  { return DeviceA;  }
  public static int    deviceB()  { return DeviceB;  }
  public static int    devicePacketKind() { return DevicePacketKind; }

  public static void initialize() {
    DeviceA          = 5;
    DeviceB          = 6;
    DevicePacketKind = 1;
    HandlerA         = 3;
    HandlerB         = 4;
    Idler            = 1;
    NoWork           = null;
    NoTask           = null;
    Worker           = 2;
    WorkPacketKind   = 2;
  }
}
