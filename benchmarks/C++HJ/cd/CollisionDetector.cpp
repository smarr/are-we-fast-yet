#include "Collision.cpp"
#include "Aircraft.h"
#include "Vector2D.h"
#include "RedBlackTree.cpp"
#include "Motion.cpp"
#include "../som/Vector.cpp"
using namespace std;

namespace CD {
    class CollisionDetector {

        private:

            shared_ptr<RedBlackTree<shared_ptr<CallSign>, shared_ptr<Vector3D>>> _state;
            shared_ptr<Vector2D> _horizontal = make_shared<Vector2D>(Constants::GOOD_VOXEL_SIZE, 0.0);
            shared_ptr<Vector2D> _vertical = make_shared<Vector2D>(0.0, Constants::GOOD_VOXEL_SIZE);

            static bool isInVoxel(shared_ptr<Vector2D> voxel, shared_ptr<Motion> motion) {
                if (voxel->_x > Constants::MAX_X ||
                    voxel->_x < Constants::MIN_X ||
                    voxel->_y > Constants::MAX_Y ||
                    voxel->_y < Constants::MIN_Y)
                    return false;
                
                shared_ptr<Vector3D> init = motion->_posOne;
                shared_ptr<Vector3D> fin = motion->_posTwo;

                double v_s = Constants::GOOD_VOXEL_SIZE;
                double r = Constants::PROXIMITY_RADIUS / 2.0;

                double v_x = voxel->_x;
                double x0 = init->_x;
                double xv = fin->_x - init->_x;

                double v_y = voxel->_y;
                double y0 = init->_y;
                double yv = fin->_y - init->_y;

                double low_x = (v_x - r - x0) / xv;
                double high_x = (v_x + v_s + r - x0) / xv;

                if (xv < 0.0) {
                    double tmp = low_x;
                    low_x = high_x;
                    high_x = tmp;
                }

                double low_y  = (v_y - r - y0) / yv;
                double high_y = (v_y + v_s + r - y0) / yv;

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
            };

            static void putIntoMap(shared_ptr<RedBlackTree<shared_ptr<Vector2D>, shared_ptr<Vector<shared_ptr<Motion>>>>> voxelMap, shared_ptr<Vector2D> voxel, shared_ptr<Motion> motion) {
                shared_ptr<Vector<shared_ptr<Motion>>> array = voxelMap->getPtr(voxel);
                if (array == nullptr) {
                    array = make_shared<Vector<shared_ptr<Motion>>>();
                    voxelMap->putPtr(voxel, array);
                }
                array->append(motion);
            }

            void recurse(shared_ptr<RedBlackTree<shared_ptr<Vector2D>, shared_ptr<Vector<shared_ptr<Motion>>>>> voxelMap, shared_ptr<RedBlackTree<shared_ptr<Vector2D>, 
                        bool>> seen, shared_ptr<Vector2D> nextVoxel, shared_ptr<Motion> motion) {
                if (!isInVoxel(nextVoxel, motion)) {
                    return;
                }
                if (seen->put(nextVoxel, true) == true) {
                    return;
                }
                putIntoMap(voxelMap, nextVoxel, motion);

                recurse(voxelMap, seen, nextVoxel->minus(_horizontal), motion);
                recurse(voxelMap, seen, nextVoxel->plus(_horizontal), motion);
                recurse(voxelMap, seen, nextVoxel->minus(_vertical), motion);
                recurse(voxelMap, seen, nextVoxel->plus(_vertical), motion);
                recurse(voxelMap, seen, nextVoxel->minus(_horizontal)->minus(_vertical), motion);
                recurse(voxelMap, seen, nextVoxel->minus(_horizontal)->plus(_vertical), motion);
                recurse(voxelMap, seen, nextVoxel->plus(_horizontal)->minus(_vertical), motion);
                recurse(voxelMap, seen, nextVoxel->plus(_horizontal)->plus(_vertical), motion);
            }

            shared_ptr<Vector<shared_ptr<Vector<shared_ptr<Motion>>>>> reduceCollisionSet(shared_ptr<Vector<shared_ptr<Motion>>> motions) {
                shared_ptr<RedBlackTree<shared_ptr<Vector2D>, shared_ptr<Vector<shared_ptr<Motion>>>>> voxelMap = make_shared<RedBlackTree<shared_ptr<Vector2D>, shared_ptr<Vector<shared_ptr<Motion>>>>>();
                motions->forEach([&](shared_ptr<Motion> motion) -> void {
                    drawMotionOnVoxelMap(voxelMap, motion);
                });

                shared_ptr<Vector<shared_ptr<Vector<shared_ptr<Motion>>>>> result = make_shared<Vector<shared_ptr<Vector<shared_ptr<Motion>>>>>();

                voxelMap->forEach([&result](shared_ptr<RedBlackTree<shared_ptr<Vector2D>, shared_ptr<Vector<shared_ptr<Motion>>>>::Entry> e) -> void { 
                    if (e->_value->size() > 1) {
                        result->append(e->_value);
                }});
                return result;
            }

            shared_ptr<Vector2D> voxelHash(shared_ptr<Vector3D> position) {
                int xDiv = (int) (position->_x / Constants::GOOD_VOXEL_SIZE);
                int yDiv = (int) (position->_y / Constants::GOOD_VOXEL_SIZE);

                double x = Constants::GOOD_VOXEL_SIZE * xDiv;
                double y = Constants::GOOD_VOXEL_SIZE * yDiv;

                if (position->_x < 0) {
                    x -= Constants::GOOD_VOXEL_SIZE;
                }
                if (position->_y < 0) {
                    y -= Constants::GOOD_VOXEL_SIZE;
                }

                return make_shared<Vector2D>(x, y);
            }

            void drawMotionOnVoxelMap(shared_ptr<RedBlackTree<shared_ptr<Vector2D>, shared_ptr<Vector<shared_ptr<Motion>>>>> voxelMap, shared_ptr<Motion> motion) {
                shared_ptr<RedBlackTree<shared_ptr<Vector2D>, bool>> seen = make_shared<RedBlackTree<shared_ptr<Vector2D>, bool>>();
                recurse(voxelMap, seen, voxelHash(motion->_posOne), motion);
            }

        public:

            CollisionDetector() {
                _state = make_shared<RedBlackTree<shared_ptr<CallSign>, shared_ptr<Vector3D>>>();
            }

            shared_ptr<Vector<shared_ptr<Collision>>> handleNewFrame(shared_ptr<Vector<shared_ptr<Aircraft>>> frame) {
                shared_ptr<Vector<shared_ptr<Motion>>> motions = make_shared<Vector<shared_ptr<Motion>>>();
                shared_ptr<RedBlackTree<shared_ptr<CallSign>, bool>> seen = make_shared<RedBlackTree<shared_ptr<CallSign>, bool>>();
                frame->forEach([&](shared_ptr<Aircraft> aircraft) -> void {
                    shared_ptr<Vector3D> oldPosition = _state->putPtr(aircraft->_callsign, aircraft->_position);
                    shared_ptr<Vector3D> newPosition = aircraft->_position;
                    seen->put(aircraft->_callsign, true);
                    if (oldPosition == nullptr) {
                        // Treat newly introduced aircraft as if they were stationary.
                        oldPosition = newPosition;
                    }
                    motions->append(make_shared<Motion>(aircraft->_callsign, oldPosition, newPosition));
                });

                shared_ptr<Vector<shared_ptr<CallSign>>> toRemove = make_shared<Vector<shared_ptr<CallSign>>>();
                _state->forEach([&seen, &toRemove](shared_ptr<RedBlackTree<shared_ptr<CallSign>, shared_ptr<Vector3D>>::Entry> e) -> void {
                    if (!seen->get(e->_key)) {
                        toRemove->append(e->_key);
                }});
                
                toRemove->forEach([&](shared_ptr<CallSign> e) -> void {
                    _state->remove(e);
                });

                shared_ptr<Vector<shared_ptr<Vector<shared_ptr<Motion>>>>> allReduced = reduceCollisionSet(motions);
                shared_ptr<Vector<shared_ptr<Collision>>> collisions = make_shared<Vector<shared_ptr<Collision>>>();

                allReduced->forEach([&](shared_ptr<Vector<shared_ptr<Motion>>> reduced) -> void {
                    for (long unsigned int i = 0; i < reduced->size(); ++i) {
                        shared_ptr<Motion> motion1 = reduced->atPtr(i);
                        for (long unsigned int j = i + 1; j < reduced->size(); ++j) {
                            shared_ptr<Motion> motion2 = reduced->atPtr(j);
                            shared_ptr<Vector3D> collision = motion1->findIntersection(motion2);
                            if (collision != nullptr) {
                                collisions->append(make_shared<Collision>(motion1->_callsign, motion2->_callsign, collision));
                            }
                        }
                    }   
                });
                return collisions;
            };

    };
};