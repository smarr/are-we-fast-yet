package cd;


public final class Collision {
  public final CallSign aircraftA;
  public final CallSign aircraftB;
  public final Vector3D position;

  public Collision(final CallSign aircraftA, final CallSign aircraftB,
      final Vector3D position) {
    this.aircraftA = aircraftA;
    this.aircraftB = aircraftB;
    this.position = position;
  }
}
