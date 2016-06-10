package cd;

import som.Vector;

public final class CollisionDetector {
  private final RedBlackTree<CallSign, Vector3D> state;

  public CollisionDetector() {
    state = new RedBlackTree<>();
  }

  public Vector<Collision> handleNewFrame(final Vector<Aircraft> frame) {
    Vector<Motion> motions = new Vector<>();
    RedBlackTree<CallSign, Boolean> seen = new RedBlackTree<>();

    frame.forEach(aircraft -> {
      Vector3D oldPosition = state.put(aircraft.callsign, aircraft.position);
      Vector3D newPosition = aircraft.position;
      seen.put(aircraft.callsign, true);

      if (oldPosition == null) {
        // Treat newly introduced aircraft as if they were stationary.
        oldPosition = newPosition;
      }

      motions.append(new Motion(aircraft.callsign, oldPosition, newPosition));
    });

    // Remove aircraft that are no longer present.
    Vector<CallSign> toRemove = new Vector<>();
    state.forEach(e -> {
      if (!seen.get(e.key)) {
        toRemove.append(e.key);
      }
    });

    toRemove.forEach(e -> state.remove(e));

    Vector<Vector<Motion>> allReduced = reduceCollisionSet(motions);
    Vector<Collision> collisions = new Vector<>();
    allReduced.forEach(reduced -> {
      for (int i = 0; i < reduced.size(); ++i) {
        Motion motion1 = reduced.at(i);
        for (int j = i + 1; j < reduced.size(); ++j) {
          Motion motion2 = reduced.at(j);
          Vector3D collision = motion1.findIntersection(motion2);
          if (collision != null) {
            collisions.append(new Collision(motion1.callsign, motion2.callsign, collision));
          }
        }
      }
    });

    return collisions;
  }

  private static boolean isInVoxel(final Vector2D voxel, final Motion motion) {
    if (voxel.x > Constants.MAX_X ||
        voxel.x < Constants.MIN_X ||
        voxel.y > Constants.MAX_Y ||
        voxel.y < Constants.MIN_Y) {
      return false;
    }

    Vector3D init = motion.posOne;
    Vector3D fin  = motion.posTwo;

    double v_s = Constants.GOOD_VOXEL_SIZE;
    double r   = Constants.PROXIMITY_RADIUS / 2.0;

    double v_x = voxel.x;
    double x0 = init.x;
    double xv = fin.x - init.x;

    double v_y = voxel.y;
    double y0 = init.y;
    double yv = fin.y - init.y;

    double low_x;
    double high_x;
    low_x = (v_x - r - x0) / xv;
    high_x = (v_x + v_s + r - x0) / xv;

    if (xv < 0.0) {
      double tmp = low_x;
      low_x = high_x;
      high_x = tmp;
    }

    double low_y;
    double high_y;
    low_y  = (v_y - r - y0) / yv;
    high_y = (v_y + v_s + r - y0) / yv;

    if (yv < 0.0) {
      double tmp = low_y;
      low_y = high_y;
      high_y = tmp;
    }

    return (((xv == 0.0 && v_x <= x0 + r && x0 - r <= v_x + v_s) /* no motion in x */ ||
             (low_x <= 1.0 && 1.0 <= high_x) || (low_x <= 0.0 && 0.0 <= high_x) ||
              (0.0 <= low_x && high_x <= 1.0)) &&
            ((yv == 0.0 && v_y <= y0 + r && y0 - r <= v_y + v_s) /* no motion in y */ ||
             ((low_y <= 1.0 && 1.0 <= high_y) || (low_y <= 0.0 && 0.0 <= high_y) ||
              (0.0 <= low_y && high_y <= 1.0))) &&
            (xv == 0.0 || yv == 0.0 || /* no motion in x or y or both */
             (low_y <= high_x && high_x <= high_y) ||
             (low_y <= low_x && low_x <= high_y) ||
             (low_x <= low_y && high_y <= high_x)));
  }

  private static final Vector2D horizontal = new Vector2D(Constants.GOOD_VOXEL_SIZE, 0.0);
  private static final Vector2D vertical   = new Vector2D(0.0, Constants.GOOD_VOXEL_SIZE);

  private static void putIntoMap(
      final RedBlackTree<Vector2D, Vector<Motion>> voxelMap,
      final Vector2D voxel, final Motion motion) {
    Vector<Motion> array = voxelMap.get(voxel);
    if (array == null) {
      array = new Vector<>();
      voxelMap.put(voxel, array);
    }
    array.append(motion);
  }

  private static void recurse(
      final RedBlackTree<Vector2D, Vector<Motion>> voxelMap,
      final RedBlackTree<Vector2D, Boolean> seen,
      final Vector2D nextVoxel, final Motion motion) {
    if (!isInVoxel(nextVoxel, motion)) {
      return;
    }

    if (seen.put(nextVoxel, true) == Boolean.TRUE) {
      return;
    }

    putIntoMap(voxelMap, nextVoxel, motion);

    recurse(voxelMap, seen, nextVoxel.minus(horizontal), motion);
    recurse(voxelMap, seen, nextVoxel.plus(horizontal), motion);
    recurse(voxelMap, seen, nextVoxel.minus(vertical), motion);
    recurse(voxelMap, seen, nextVoxel.plus(vertical), motion);
    recurse(voxelMap, seen, nextVoxel.minus(horizontal).minus(vertical), motion);
    recurse(voxelMap, seen, nextVoxel.minus(horizontal).plus(vertical), motion);
    recurse(voxelMap, seen, nextVoxel.plus(horizontal).minus(vertical), motion);
    recurse(voxelMap, seen, nextVoxel.plus(horizontal).plus(vertical), motion);
  }

  private static Vector<Vector<Motion>> reduceCollisionSet(final Vector<Motion> motions) {
    RedBlackTree<Vector2D, Vector<Motion>> voxelMap = new RedBlackTree<>();
    motions.forEach(motion -> drawMotionOnVoxelMap(voxelMap, motion));

    Vector<Vector<Motion>> result = new Vector<>();
    voxelMap.forEach(e -> {
      if (e.value.size() > 1) {
        result.append(e.value);
      }
    });
    return result;
  }

  private static Vector2D voxelHash(final Vector3D position) {
    int xDiv = (int) (position.x / Constants.GOOD_VOXEL_SIZE);
    int yDiv = (int) (position.y / Constants.GOOD_VOXEL_SIZE);

    double x = Constants.GOOD_VOXEL_SIZE * xDiv;
    double y = Constants.GOOD_VOXEL_SIZE * yDiv;

    if (position.x < 0) {
      x -= Constants.GOOD_VOXEL_SIZE;
    }
    if (position.y < 0) {
      y -= Constants.GOOD_VOXEL_SIZE;
    }

    return new Vector2D(x, y);
  }

  private static void drawMotionOnVoxelMap(
      final RedBlackTree<Vector2D, Vector<Motion>> voxelMap, final Motion motion) {
    RedBlackTree<Vector2D, Boolean> seen = new RedBlackTree<>();
    recurse(voxelMap, seen, voxelHash(motion.posOne), motion);
  }
}
