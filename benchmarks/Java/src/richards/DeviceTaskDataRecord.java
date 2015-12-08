package richards;


public class DeviceTaskDataRecord extends RBObject {
  private Packet pending;
  public Packet getPending() { return pending; }
  public void setPending(final Packet packet) { pending = packet; }

  private DeviceTaskDataRecord() {
    pending = RBObject.noWork();
  }

  public static DeviceTaskDataRecord create() {
    return new DeviceTaskDataRecord();
  }
}
