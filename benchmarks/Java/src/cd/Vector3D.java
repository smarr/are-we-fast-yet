package cd;


public final class Vector3D {
  public final double x;
  public final double y;
  public final double z;

  public Vector3D(final double x, final double y, final double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Vector3D plus(final Vector3D other) {
    return new Vector3D(x + other.x,
                        y + other.y,
                        z + other.z);
  }

  public Vector3D minus(final Vector3D other) {
    return new Vector3D(x - other.x,
                        y - other.y,
                        z - other.z);
  }

  public double dot(final Vector3D other) {
    return x * other.x + y * other.y + z * other.z;
  }

  public double squaredMagnitude() {
    return this.dot(this);
  }

  public double magnitude() {
    return Math.sqrt(squaredMagnitude());
  }

  public Vector3D times(final double amount) {
    return new Vector3D(x * amount,
                        y * amount,
                        z * amount);
  }
}
