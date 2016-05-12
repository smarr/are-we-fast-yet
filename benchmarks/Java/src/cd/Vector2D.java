package cd;


public final class Vector2D implements Comparable<Vector2D> {
  public final double x;
  public final double y;

  public Vector2D(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  public Vector2D plus(final Vector2D other) {
    return new Vector2D(x + other.x,
                        y + other.y);
  }

  public Vector2D minus(final Vector2D other) {
    return new Vector2D(x - other.x,
                        y - other.y);
  }

  @Override
  public int compareTo(final Vector2D other) {
    int result = compareNumbers(this.x, other.x);
    if (result != 0) {
      return result;
    }
    return compareNumbers(this.y, other.y);
  }

  private static int compareNumbers(final double a, final double b) {
    if (a == b) {
      return 0;
    }
    if (a < b) {
      return -1;
    }
    if (a > b) {
      return 1;
    }

    // We say that NaN is smaller than non-NaN.
    if (a == a) {
      return 1;
    }
    return -1;
  }
}
