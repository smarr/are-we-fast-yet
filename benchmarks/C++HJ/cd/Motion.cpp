#include "CallSign.h"
#include "Vector3D.h"
#include "Constants.h"
#include <memory>
#include <iostream>

using namespace std;

namespace CD {
    class Motion {
        private:

            shared_ptr<Vector3D> delta() const {
                return _posTwo->minus(_posOne);
            }

        public:
            shared_ptr<CallSign> _callsign;
            shared_ptr<Vector3D> _posOne;
            shared_ptr<Vector3D> _posTwo;

            Motion(shared_ptr<CallSign> callsign, shared_ptr<Vector3D> posOne, shared_ptr<Vector3D> posTwo) {
                _callsign = callsign;
                _posOne = posOne;
                _posTwo = posTwo;
            }

            shared_ptr<Vector3D> findIntersection(shared_ptr<Motion> other) {
                shared_ptr<Vector3D> init1 = _posOne;
                shared_ptr<Vector3D> init2 = other->_posOne;
                shared_ptr<Vector3D> vec1 = delta();
                shared_ptr<Vector3D> vec2 = other->delta();
                double radius = Constants::PROXIMITY_RADIUS;

                // this test is not geometrical 3-d intersection test, it takes the fact that the aircraft move
                // into account ; so it is more like a 4d test
                // (it assumes that both of the aircraft have a constant speed over the tested interval)

                // we thus have two points, each of them moving on its line segment at constant speed ; we are looking
                // for times when the distance between these two points is smaller than r

                // vec1 is vector of aircraft 1
                // vec2 is vector of aircraft 2

                // a = (V2 - V1)^T * (V2 - V1)
                double a = vec2->minus(vec1)->squaredMagnitude();

                if (a != 0.0) {
                    // we are first looking for instances of time when the planes are exactly r from each other
                    // at least one plane is moving ; if the planes are moving in parallel, they do not have constant speed

                    // if the planes are moving in parallel, then
                    //   if the faster starts behind the slower, we can have 2, 1, or 0 solutions
                    //   if the faster plane starts in front of the slower, we can have 0 or 1 solutions

                    // if the planes are not moving in parallel, then

                    // point P1 = I1 + vV1
                    // point P2 = I2 + vV2
                    //   - looking for v, such that dist(P1,P2) = || P1 - P2 || = r

                    // it follows that || P1 - P2 || = sqrt( < P1-P2, P1-P2 > )
                    //   0 = -r^2 + < P1 - P2, P1 - P2 >
                    //  from properties of dot product
                    //   0 = -r^2 + <I1-I2,I1-I2> + v * 2<I1-I2, V1-V2> + v^2 *<V1-V2,V1-V2>
                    //   so we calculate a, b, c - and solve the quadratic equation
                    //   0 = c + bv + av^2

                    // b = 2 * <I1-I2, V1-V2>
                    double b = 2.0 * init1->minus(init2)->dot(vec1->minus(vec2));

                    // c = -r^2 + (I2 - I1)^T * (I2 - I1)
                    double c = -radius * radius + init2->minus(init1)->squaredMagnitude();

                    double discr = b * b - 4.0 * a * c;
                    if (discr < 0.0) {
                        return nullptr;
                    }

                    double v1 = (-b - sqrt(discr)) / (2.0 * a);
                    double v2 = (-b + sqrt(discr)) / (2.0 * a);

                    if (v1 <= v2 && ((v1  <= 1.0 && 1.0 <= v2) ||
                                    (v1  <= 0.0 && 0.0 <= v2) ||
                                    (0.0 <= v1  && v2  <= 1.0))) {
                        // Pick a good "time" at which to report the collision.
                        double v;
                        if (v1 <= 0.0) {
                            // The collision started before this frame. Report it at the start of the frame.
                            v = 0.0;
                        } else {
                            // The collision started during this frame. Report it at that moment.
                            v = v1;
                        }

                        shared_ptr<Vector3D> result1 = init1->plus(vec1->times(v));
                        shared_ptr<Vector3D> result2 = init2->plus(vec2->times(v));

                        shared_ptr<Vector3D> result = result1->plus(result2)->times(0.5);
                        if (result->_x >= Constants::MIN_X &&
                            result->_x <= Constants::MAX_X &&
                            result->_y >= Constants::MIN_Y &&
                            result->_y <= Constants::MAX_Y &&
                            result->_z >= Constants::MIN_Z &&
                            result->_z <= Constants::MAX_Z) {
                            return result;
                        }
                    }

                    return nullptr;
                }
                // the planes have the same speeds and are moving in parallel (or they are not moving at all)
                // they  thus have the same distance all the time ; we calculate it from the initial point

                // dist = || i2 - i1 || = sqrt(  ( i2 - i1 )^T * ( i2 - i1 ) )
                double dist = init2->minus(init1)->magnitude();
                if (dist <= radius) {
                    return init1->plus(init2)->times(0.5);
                }

                return nullptr;
            }
    };
};