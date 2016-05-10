package cd;

import som.Vector;


public final class Simulator {
  private final Vector<CallSign> aircraft;

  public Simulator(final int numAircraft) {
    aircraft = new Vector<>();
    for (int i = 0; i < numAircraft; i++) {
      aircraft.append(new CallSign(i));
    }
  }

  public Vector<Aircraft> simulate(final double time) {
    Vector<Aircraft> frame = new Vector<>();
    for (int i = 0; i < aircraft.size(); i += 2) {
      frame.append(new Aircraft(aircraft.at(i),
          new Vector3D(time, Math.cos(time) * 2 + i * 3, 10)));
      frame.append(new Aircraft(aircraft.at(i + 1),
          new Vector3D(time, Math.sin(time) * 2 + i * 3, 10)));
    }
    return frame;
  }
}
