package nbody;


final class Body {
  private static final double PI = 3.141592653589793;
  private static final double SOLAR_MASS = 4 * PI * PI;
  private static final double DAYS_PER_YER = 365.24;

  private double x, y, z, vx, vy, vz, mass;

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

  public void setMass(final double val) { mass = val; }

  Body offsetMomentum(final double px, final double py, final double pz) {
    vx = 0.0 - (px / SOLAR_MASS);
    vy = 0.0 - (py / SOLAR_MASS);
    vz = 0.0 - (pz / SOLAR_MASS);
    return this;
  }

  public Body() {}

  static Body jupiter() {
    Body p = new Body();
    p.setX(4.84143144246472090e+00);
    p.setY(-1.16032004402742839e+00);
    p.setZ(-1.03622044471123109e-01);
    p.setVX(1.66007664274403694e-03   * DAYS_PER_YER);
    p.setVY(7.69901118419740425e-03   * DAYS_PER_YER);
    p.setVZ(-6.90460016972063023e-05  * DAYS_PER_YER);
    p.setMass(9.54791938424326609e-04 * SOLAR_MASS);
    return p;
  }

  static Body saturn() {
    Body p = new Body();
    p.setX(8.34336671824457987e+00);
    p.setY(4.12479856412430479e+00);
    p.setZ(-4.03523417114321381e-01);
    p.setVX(-2.76742510726862411e-03  * DAYS_PER_YER);
    p.setVY(4.99852801234917238e-03   * DAYS_PER_YER);
    p.setVZ(2.30417297573763929e-05   * DAYS_PER_YER);
    p.setMass(2.85885980666130812e-04 * SOLAR_MASS);
    return p;
  }

  static Body uranus() {
    Body p = new Body();
    p.setX(1.28943695621391310e+01);
    p.setY(-1.51111514016986312e+01);
    p.setZ(-2.23307578892655734e-01);
    p.setVX(2.96460137564761618e-03   * DAYS_PER_YER);
    p.setVY(2.37847173959480950e-03   * DAYS_PER_YER);
    p.setVZ(-2.96589568540237556e-05  * DAYS_PER_YER);
    p.setMass(4.36624404335156298e-05 * SOLAR_MASS);
    return p;
  }

  static Body neptune() {
    Body p = new Body();
    p.setX(1.53796971148509165e+01);
    p.setY(-2.59193146099879641e+01);
    p.setZ(1.79258772950371181e-01);
    p.setVX(2.68067772490389322e-03   * DAYS_PER_YER);
    p.setVY(1.62824170038242295e-03   * DAYS_PER_YER);
    p.setVZ(-9.51592254519715870e-05  * DAYS_PER_YER);
    p.setMass(5.15138902046611451e-05 * SOLAR_MASS);
    return p;
  }

  static Body sun() {
    Body p = new Body();
    p.setMass(SOLAR_MASS);
    return p;
  }
}
