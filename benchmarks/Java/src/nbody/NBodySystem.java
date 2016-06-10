/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted basde on the SOM version.
 */
package nbody;

public class NBodySystem {
  private final Body[] bodies;

  public NBodySystem() {
    bodies = createBodies();
  }

  public Body[] createBodies() {
    Body[] bodies = new Body[] {Body.sun(),
                                Body.jupiter(),
                                Body.saturn(),
                                Body.uranus(),
                                Body.neptune()};

    double px = 0.0;
    double py = 0.0;
    double pz = 0.0;

    for (Body b : bodies) {
      px += b.getVX() * b.getMass();
      py += b.getVY() * b.getMass();
      pz += b.getVZ() * b.getMass();
    }

    bodies[0].offsetMomentum(px, py, pz);

    return bodies;
  }

  public void advance(final double dt) {

    for (int i = 0; i < bodies.length; ++i) {
      Body iBody = bodies[i];

      for (int j = i + 1; j < bodies.length; ++j) {
        Body jBody = bodies[j];
        double dx = iBody.getX() - jBody.getX();
        double dy = iBody.getY() - jBody.getY();
        double dz = iBody.getZ() - jBody.getZ();

        double dSquared = dx * dx + dy * dy + dz * dz;
        double distance = Math.sqrt(dSquared);
        double mag = dt / (dSquared * distance);

        iBody.setVX(iBody.getVX() - (dx * jBody.getMass() * mag));
        iBody.setVY(iBody.getVY() - (dy * jBody.getMass() * mag));
        iBody.setVZ(iBody.getVZ() - (dz * jBody.getMass() * mag));

        jBody.setVX(jBody.getVX() + (dx * iBody.getMass() * mag));
        jBody.setVY(jBody.getVY() + (dy * iBody.getMass() * mag));
        jBody.setVZ(jBody.getVZ() + (dz * iBody.getMass() * mag));
      }
    }

    for (Body body : bodies) {
      body.setX(body.getX() + dt * body.getVX());
      body.setY(body.getY() + dt * body.getVY());
      body.setZ(body.getZ() + dt * body.getVZ());
    }
  }

  public double energy() {
    double dx;
    double dy;
    double dz;
    double distance;
    double e = 0.0;

    for (int i = 0; i < bodies.length; ++i) {
      Body iBody = bodies[i];
      e += 0.5 * iBody.getMass()
          * (iBody.getVX() * iBody.getVX() +
             iBody.getVY() * iBody.getVY() +
             iBody.getVZ() * iBody.getVZ());

      for (int j = i + 1; j < bodies.length; ++j) {
        Body jBody = bodies[j];
        dx = iBody.getX() - jBody.getX();
        dy = iBody.getY() - jBody.getY();
        dz = iBody.getZ() - jBody.getZ();

        distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        e -= (iBody.getMass() * jBody.getMass()) / distance;
      }
    }
    return e;
  }
}
