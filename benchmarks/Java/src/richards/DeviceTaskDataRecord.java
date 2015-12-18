package richards;


final class DeviceTaskDataRecord extends RBObject {
  private Packet pending;

  DeviceTaskDataRecord() {
    pending = NO_WORK;
  }

  public Packet getPending() { return pending; }
  public void setPending(final Packet packet) { pending = packet; }
}
