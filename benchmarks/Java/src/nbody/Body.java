/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted basde on the SOM version.
 */
package nbody;

final class Body {
  private static final double PI = 3.141592653589793;
  private static final double SOLAR_MASS = 4 * PI * PI;
  private static final double DAYS_PER_YER = 365.24;

  private double x;
  private double y;
  private double z;
  private double vx;
  private double vy;
  private double vz;
  private final double mass;

  public double getX() { return x; }
  public double getY() { return y; }
  public double getZ() { return z; }

  public double getVX() { return vx; }
  public double getVY() { return vy; }
  public double getVZ() { return vz; }

  public double getMass() { return mass; }

  public void setX(final double x) { this.x = x; }
  public void setY(final double y) { this.y = y; }
  public void setZ(final double z) { this.z = z; }

  public void setVX(final double vx) { this.vx = vx; }
  public void setVY(final double vy) { this.vy = vy; }
  public void setVZ(final double vz) { this.vz = vz; }

  void offsetMomentum(final double px, final double py, final double pz) {
    vx = 0.0 - (px / SOLAR_MASS);
    vy = 0.0 - (py / SOLAR_MASS);
    vz = 0.0 - (pz / SOLAR_MASS);
  }

  Body(final double x, final double y, final double z,
      final double vx, final double vy, final double vz, final double mass) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.vx = vx * DAYS_PER_YER;
    this.vy = vy * DAYS_PER_YER;
    this.vz = vz * DAYS_PER_YER;
    this.mass = mass * SOLAR_MASS;
  }

  static Body jupiter() {
    return new Body(
         4.84143144246472090e+00,
        -1.16032004402742839e+00,
        -1.03622044471123109e-01,
         1.66007664274403694e-03,
         7.69901118419740425e-03,
        -6.90460016972063023e-05,
         9.54791938424326609e-04);
  }

  static Body saturn() {
    return new Body(
         8.34336671824457987e+00,
         4.12479856412430479e+00,
        -4.03523417114321381e-01,
        -2.76742510726862411e-03,
         4.99852801234917238e-03,
         2.30417297573763929e-05,
         2.85885980666130812e-04);
  }

  static Body uranus() {
    return new Body(
         1.28943695621391310e+01,
        -1.51111514016986312e+01,
        -2.23307578892655734e-01,
         2.96460137564761618e-03,
         2.37847173959480950e-03,
        -2.96589568540237556e-05,
         4.36624404335156298e-05);
  }

  static Body neptune() {
    return new Body(
         1.53796971148509165e+01,
        -2.59193146099879641e+01,
         1.79258772950371181e-01,
         2.68067772490389322e-03,
         1.62824170038242295e-03,
        -9.51592254519715870e-05,
         5.15138902046611451e-05);
  }

  static Body sun() {
    return new Body(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
  }
}
