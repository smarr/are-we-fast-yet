#ifndef COLLISIONDETECTOR
#define COLLISIONDETECTOR

#include "../som/Vector.cpp"
#include "Aircraft.h"
#include "Collision.h"
#include "Motion.h"
#include "RedBlackTree.cpp"
#include "Vector2D.h"

using namespace std;

namespace CD {
class CollisionDetector {
 private:
  shared_ptr<RedBlackTree<shared_ptr<CallSign>, shared_ptr<Vector3D>>> _state;
  shared_ptr<Vector2D> _horizontal =
      make_shared<Vector2D>(Constants::GOOD_VOXEL_SIZE, 0.0);
  shared_ptr<Vector2D> _vertical =
      make_shared<Vector2D>(0.0, Constants::GOOD_VOXEL_SIZE);

  static bool isInVoxel(shared_ptr<Vector2D> voxel, shared_ptr<Motion> motion);
  static void putIntoMap(
      shared_ptr<RedBlackTree<shared_ptr<Vector2D>,
                              shared_ptr<Vector<shared_ptr<Motion>>>>> voxelMap,
      shared_ptr<Vector2D> voxel,
      shared_ptr<Motion> motion);
  void recurse(
      shared_ptr<RedBlackTree<shared_ptr<Vector2D>,
                              shared_ptr<Vector<shared_ptr<Motion>>>>> voxelMap,
      shared_ptr<RedBlackTree<shared_ptr<Vector2D>, bool>> seen,
      shared_ptr<Vector2D> nextVoxel,
      shared_ptr<Motion> motion);
  shared_ptr<Vector<shared_ptr<Vector<shared_ptr<Motion>>>>> reduceCollisionSet(
      shared_ptr<Vector<shared_ptr<Motion>>> motions);
  shared_ptr<Vector2D> voxelHash(shared_ptr<Vector3D> position);
  void drawMotionOnVoxelMap(
      shared_ptr<RedBlackTree<shared_ptr<Vector2D>,
                              shared_ptr<Vector<shared_ptr<Motion>>>>> voxelMap,
      shared_ptr<Motion> motion);

 public:
  CollisionDetector();
  shared_ptr<Vector<shared_ptr<Collision>>> handleNewFrame(
      shared_ptr<Vector<shared_ptr<Aircraft>>> frame);
};
};  // namespace CD

#endif  // COLLISIONDETECTOR