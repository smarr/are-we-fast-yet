package cd;


public class Aircraft {
  public final CallSign callsign;
  public final Vector3D position;

  public Aircraft(final CallSign callsign, final Vector3D position) {
    this.callsign = callsign;
    this.position = position;
  }
}
