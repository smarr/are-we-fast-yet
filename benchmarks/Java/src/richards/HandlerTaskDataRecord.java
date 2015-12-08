package som.richards;


public class HandlerTaskDataRecord extends RBObject {
  private Packet workIn;
  private Packet deviceIn;

  public Packet deviceIn() { return deviceIn; }
  public void deviceIn(final Packet aPacket) { deviceIn = aPacket; }

  public void deviceInAdd(final Packet packet) {
    deviceIn = append(packet, deviceIn);
  }

  public Packet workIn() { return workIn; }
  public void workIn(final Packet aWorkQueue) { workIn = aWorkQueue; }

  public void workInAdd(final Packet packet) {
    workIn = append(packet, workIn);
  }

  private HandlerTaskDataRecord() {
    workIn = deviceIn = RBObject.noWork();
  }

  public static HandlerTaskDataRecord create() {
    return new HandlerTaskDataRecord();
  }
}
