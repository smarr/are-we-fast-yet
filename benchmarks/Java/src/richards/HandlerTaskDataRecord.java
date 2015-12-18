package richards;


final class HandlerTaskDataRecord extends RBObject {
  private Packet workIn;
  private Packet deviceIn;

  HandlerTaskDataRecord() {
    workIn = deviceIn = NO_WORK;
  }

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
}
