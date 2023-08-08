/*
 * Copyright (c) 2001-2016 Stefan Marr
 * Copyright (c) 2023 Rochus Keller <me@rochus-keller.ch> (for C++ migration)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the 'Software'), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

#include "CD.h"
#include "RedBlackTree.h"
#include <iostream>
#include <math.h>
#include "som/Vector.h"
using namespace som;

class Vector2D {
public:
    double x;
    double y;

    Vector2D(double x = 0, double y = 0) {
        this->x = x;
        this->y = y;
    }

    Vector2D plus(const Vector2D& other) const {
        return Vector2D(x + other.x,
                        y + other.y);
    }

    Vector2D minus(const Vector2D& other) const {
        return Vector2D(x - other.x,
                        y - other.y);
    }

    class Compare
    {
        static int compareNumbers( double a,  double b) {
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

    public:
        int operator()(const Vector2D& lhs, const Vector2D& other) {
            int result = compareNumbers(lhs.x, other.x);
            if (result != 0) {
                return result;
            }
            return compareNumbers(lhs.y, other.y);
        }
    };
};

class Vector3D {
public:
    double x;
    double y;
    double z;

    Vector3D( double x = 0,  double y = 0,  double z = 0) {
        this->x = x;
        this->y = y;
        this->z = z;
    }

    Vector3D plus(const Vector3D& other) const {
        return Vector3D(x + other.x,
                            y + other.y,
                            z + other.z);
    }

    Vector3D minus(const Vector3D& other) const {
        return Vector3D(x - other.x,
                            y - other.y,
                            z - other.z);
    }

    double dot(const Vector3D& other) const {
        return x * other.x + y * other.y + z * other.z;
    }

    double squaredMagnitude() {
        return dot(*this);
    }

    double magnitude() {
        return sqrt(squaredMagnitude());
    }

    Vector3D times( double amount) {
        return Vector3D(x * amount,
                            y * amount,
                            z * amount);
    }
};

class CallSign {
public:
    int value;
    CallSign(int value = 0) {
        this->value = value;
    }

    class Compare
    {
    public:
        int operator()(const CallSign& lhs, const CallSign& other) {
            return (lhs.value == other.value) ? 0 : ((lhs.value < other.value) ? -1 : 1);
        }
    };
};

class Aircraft {
public:
    CallSign callsign;
    Vector3D position;

    Aircraft() {}
    Aircraft(const CallSign& callsign, const Vector3D& position) {
        this->callsign = callsign;
        this->position = position;
    }
};

class Collision {
public:
    CallSign aircraftA;
    CallSign aircraftB;
    Vector3D position;

    Collision() {}
    Collision(const CallSign& aircraftA, const CallSign& aircraftB, const Vector3D& position) {
        this->aircraftA = aircraftA;
        this->aircraftB = aircraftB;
        this->position = position;
    }
};

class Simulator {
    Vector<CallSign> aircraft;
public:
    Simulator(int numAircraft) {
        for (int i = 0; i < numAircraft; i++) {
            aircraft.append(CallSign(i));
        }
    }

    void simulate(Vector<Aircraft>& frame, double time) {
        for (int i = 0; i < aircraft.size(); i += 2) {
            frame.append(Aircraft(aircraft.at(i),
                                  Vector3D(time, cos(time) * 2 + i * 3, 10)));
            frame.append(Aircraft(aircraft.at(i + 1),
                                  Vector3D(time, sin(time) * 2 + i * 3, 10)));
        }
    }
};

static const double MIN_X = 0.0;
static const double MIN_Y = 0.0;
static const double MAX_X = 1000.0;
static const double MAX_Y = 1000.0;
static const double MIN_Z = 0.0;
static const double MAX_Z = 10.0;
static const double PROXIMITY_RADIUS = 1.0;
static const double GOOD_VOXEL_SIZE  = PROXIMITY_RADIUS * 2.0;

class Motion {
    Vector3D delta() const {
        return posTwo.minus(posOne);
    }
public:
    CallSign callsign;
    Vector3D posOne;
    Vector3D posTwo;

    Motion() {}
    Motion(const CallSign& callsign, const Vector3D& posOne, const Vector3D& posTwo) {
        this->callsign = callsign;
        this->posOne = posOne;
        this->posTwo = posTwo;
    }


    bool findIntersection(const Motion& other, Vector3D& result) const {
        Vector3D init1 = posOne;
        Vector3D init2 = other.posOne;
        Vector3D vec1 = delta();
        Vector3D vec2 = other.delta();
        const double radius = PROXIMITY_RADIUS;

        // this test is not geometrical 3-d intersection test, it takes the fact that the aircraft move
        // into account ; so it is more like a 4d test
        // (it assumes that both of the aircraft have a constant speed over the tested interval)

        // we thus have two points, each of them moving on its line segment at constant speed ; we are looking
        // for times when the distance between these two points is smaller than r

        // vec1 is vector of aircraft 1
        // vec2 is vector of aircraft 2

        // a = (V2 - V1)^T * (V2 - V1)
        double a = vec2.minus(vec1).squaredMagnitude();

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
            double b = 2.0 * init1.minus(init2).dot(vec1.minus(vec2));

            // c = -r^2 + (I2 - I1)^T * (I2 - I1)
            double c = -radius * radius + init2.minus(init1).squaredMagnitude();

            double discr = b * b - 4.0 * a * c;
            if (discr < 0.0) {
                return false;
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

                Vector3D result1 = init1.plus(vec1.times(v));
                Vector3D result2 = init2.plus(vec2.times(v));

                result = result1.plus(result2).times(0.5);
                if (result.x >= MIN_X &&
                        result.x <= MAX_X &&
                        result.y >= MIN_Y &&
                        result.y <= MAX_Y &&
                        result.z >= MIN_Z &&
                        result.z <= MAX_Z) {
                    return true;
                }
            }

            return false;
        }

        // the planes have the same speeds and are moving in parallel (or they are not moving at all)
        // they  thus have the same distance all the time ; we calculate it from the initial point

        // dist = || i2 - i1 || = sqrt(  ( i2 - i1 )^T * ( i2 - i1 ) )
        double dist = init2.minus(init1).magnitude();
        if (dist <= radius) {
            result = init1.plus(init2).times(0.5);
            return true;
        }

        return false;
    }
};

static const Vector2D horizontal(GOOD_VOXEL_SIZE, 0.0);
static const Vector2D vertical(0.0, GOOD_VOXEL_SIZE);

#define USE_FANCY_ITERATORS
// NOTE: there is no performance difference whether we use callbacks or for loops to iterate
// much much more expensive was allocation of 50 in empty Vector while using Vector by value
// 23'000us vs 1536us

class CollisionDetector {
    typedef RedBlackTree<CallSign, Vector3D, CallSign::Compare> State;
    State state;

public:
    void handleNewFrame(Vector<Aircraft>& frame, Vector<Collision>& collisions) {
        Vector<Motion> motions;
        typedef RedBlackTree<CallSign, bool, CallSign::Compare> Seen;
        Seen seen;

#ifdef USE_FANCY_ITERATORS
        class Iter1 : public ForEachInterface<Aircraft>
        {
            RedBlackTree<CallSign, bool, CallSign::Compare>& seen;
            RedBlackTree<CallSign, Vector3D, CallSign::Compare>& state;
            Vector<Motion>& motions;
        public:
            Iter1(RedBlackTree<CallSign, bool, CallSign::Compare>& s1,
                  RedBlackTree<CallSign, Vector3D, CallSign::Compare>& s2,
                  Vector<Motion>& m):seen(s1),state(s2), motions(m){}
            void apply(const Aircraft& aircraft)
            {
                Vector3D* oldPosition = state.put(aircraft.callsign, aircraft.position);
                Vector3D newPosition = aircraft.position;
                seen.put(aircraft.callsign, true);

                if (oldPosition == 0) {
                    // Treat newly introduced aircraft as if they were stationary.
                    motions.append(Motion(aircraft.callsign, newPosition, newPosition));
                }else
                {
                    motions.append(Motion(aircraft.callsign, *oldPosition, newPosition));
#if 0
                    Motion* motion1 = &m;
                    std::cout << "callsign: " << motion1->callsign.value
                              << " posone: " << motion1->posOne.x << " " << motion1->posOne.y << " " << motion1->posOne.z
                              << " postwo: " << motion1->posTwo.x << " " << motion1->posTwo.y << " " << motion1->posTwo.z
                              << std::endl;
#endif
                 }
            }
        } iter1(seen, state, motions);

        frame.forEach(iter1);
#else
        for( int i = 0; i < frame.size(); i++ )
        {
            const Aircraft& aircraft = *frame.at(i);
            Vector3D* oldPosition = state.put(aircraft.callsign, aircraft.position);
            Vector3D newPosition = aircraft.position;
            seen.put(aircraft.callsign, true);

            if (oldPosition == 0) {
                // Treat newly introduced aircraft as if they were stationary.
                motions.append(Motion(aircraft.callsign, newPosition, newPosition));
            }else
                motions.append(Motion(aircraft.callsign, *oldPosition, newPosition));
        }
#endif

        // Remove aircraft that are no longer present.
        Vector<CallSign> toRemove;

        class Iter2 : public ForEachInterface<State::Entry<CallSign, Vector3D> >
        {
            Seen& seen;
            Vector<CallSign>& toRemove;
        public:
            Iter2(Seen& s1, Vector<CallSign>& r):seen(s1), toRemove(r){}
            void apply(const State::Entry<CallSign, Vector3D>& e)
            {
                if (!seen.get(e.key)) {
                    toRemove.append(e.key);
                }
            }
        } iter2(seen,toRemove);

        state.forEach(iter2);

#ifdef USE_FANCY_ITERATORS
        class Iter3 : public ForEachInterface<CallSign>
        {
            State& state;
        public:
            Iter3(State& s):state(s){}
            void apply(const CallSign& e)
            {
                state.remove(e);
            }
        } iter3(state);

        toRemove.forEach(iter3);
#else
        for( int i = 0; i < toRemove.size(); i++ )
        {
            const CallSign& e = *toRemove.at(i);
            state.remove(e);
        }
#endif

        Vector<Vector<Motion> > allReduced;
        reduceCollisionSet(motions, allReduced);

        collisions.removeAll();

#ifdef USE_FANCY_ITERATORS
        class Iter4 : public ForEachInterface<Vector<Motion> >
        {
            Vector<Collision>& collisions;
        public:
            Iter4(Vector<Collision>& c):collisions(c){}
            void apply(const Vector<Motion>& reduced)
            {
                for (int i = 0; i < reduced.size(); ++i) {
                    const Motion& motion1 = reduced.at(i);
                    for (int j = i + 1; j < reduced.size(); ++j) {
                        const Motion& motion2 = reduced.at(j);
                        Vector3D collision;
                        const bool hit = motion1.findIntersection(motion2, collision);
                        if( hit )
                            collisions.append(Collision(motion1.callsign, motion2.callsign, collision));
                    }
                }
            }
        } iter4(collisions);

        allReduced.forEach(iter4);
#else
        for( int k = 0; k < allReduced.size(); k++ )
        {
            const Vector<Motion>& reduced = *allReduced.at(k);
            for (int i = 0; i < reduced.size(); ++i) {
                Motion* motion1 = reduced.at(i);
                for (int j = i + 1; j < reduced.size(); ++j) {
                    Motion* motion2 = reduced.at(j);
                    Vector3D collision;
                    const bool hit = motion1->findIntersection(*motion2, collision);
                    if( hit )
                        collisions.append(Collision(motion1->callsign, motion2->callsign, collision));
                }
            }
        }
#endif
    }

private:
    static bool isInVoxel(const Vector2D& voxel, const Motion& motion) {
        if (voxel.x > MAX_X ||
                voxel.x < MIN_X ||
                voxel.y > MAX_Y ||
                voxel.y < MIN_Y) {
            return false;
        }

        Vector3D init = motion.posOne;
        Vector3D fin  = motion.posTwo;

        double v_s = GOOD_VOXEL_SIZE;
        double r   = PROXIMITY_RADIUS / 2.0;

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

    typedef RedBlackTree<Vector2D, Vector<Motion>, Vector2D::Compare> VoxelMap;
    static void putIntoMap( VoxelMap& voxelMap, const Vector2D& voxel, const Motion& motion) {
        Vector<Motion>* array = voxelMap.get(voxel);
        if (array == 0) {
            Vector<Motion> a;
            a.append(motion);
            voxelMap.put(voxel,a);
        }else
            array->append(motion);
    }

    static void recurse(
            VoxelMap& voxelMap,
            RedBlackTree<Vector2D, bool, Vector2D::Compare>& seen,
            const Vector2D& nextVoxel, const Motion& motion) {
        if (!isInVoxel(nextVoxel, motion)) {
            return;
        }

        bool* res = seen.put(nextVoxel, true);
        if ( res && *res) {
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

    static void reduceCollisionSet(Vector<Motion>& motions, Vector<Vector<Motion> >& result) {
        VoxelMap voxelMap;

        result.removeAll();

        class Iter1 : public ForEachInterface<Motion>
        {
            VoxelMap& voxelMap;
        public:
            Iter1(VoxelMap& v):voxelMap(v){}
            void apply(const Motion& motion)
            {
                CollisionDetector::drawMotionOnVoxelMap(voxelMap, motion);
            }
        } iter1(voxelMap);
        motions.forEach(iter1);

        class Iter2 : public ForEachInterface<VoxelMap::Entry<Vector2D, Vector<Motion> > >
        {
            Vector<Vector<Motion> >& result;
        public:
            Iter2(Vector<Vector<Motion> >& r):result(r) {}
            void apply(const VoxelMap::Entry<Vector2D,Vector<Motion> >& e)
            {
                if (e.value.size() > 1) {
                    result.append(e.value);
                }
            }
        } iter2(result);
        voxelMap.forEach(iter2);
    }

    static Vector2D voxelHash(const Vector3D& position) {
        int xDiv = (int) (position.x / GOOD_VOXEL_SIZE);
        int yDiv = (int) (position.y / GOOD_VOXEL_SIZE);

        double x = GOOD_VOXEL_SIZE * xDiv;
        double y = GOOD_VOXEL_SIZE * yDiv;

        if (position.x < 0) {
            x -= GOOD_VOXEL_SIZE;
        }
        if (position.y < 0) {
            y -= GOOD_VOXEL_SIZE;
        }

        return Vector2D(x, y);
    }

    static void drawMotionOnVoxelMap(VoxelMap& voxelMap, const Motion& motion) {
        RedBlackTree<Vector2D, bool, Vector2D::Compare> seen;
        recurse(voxelMap, seen, voxelHash(motion.posOne), motion);
    }
};

int CD::benchmark(int numAircrafts)
{

    int numFrames = 200;

    Simulator simulator(numAircrafts);
    CollisionDetector detector;

    int actualCollisions = 0;

    for (int i = 0; i < numFrames; i++) {
        double time = i / 10.0;
        Vector<Aircraft> frame;
        simulator.simulate(frame, time);
        Vector<Collision> collisions;
        detector.handleNewFrame(frame, collisions);
        actualCollisions += collisions.size();
    }

    return actualCollisions;
}

bool CD::verifyResult(int actualCollisions, int numAircrafts)
{
    if (numAircrafts == 1000) { return actualCollisions == 14484; }
    if (numAircrafts ==  500) { return actualCollisions == 14484; }
    if (numAircrafts ==  250) { return actualCollisions == 10830; }
    if (numAircrafts ==  200) { return actualCollisions ==  8655; }
    if (numAircrafts ==  100) { return actualCollisions ==  4305; }
    if (numAircrafts ==   10) { return actualCollisions ==   390; }
    if (numAircrafts ==    2) { return actualCollisions ==    42; }

    // Checkstyle: stop
    std::cerr << "No verification result for " << numAircrafts << " found" << std::endl;
    std::cerr << "Result is: " << actualCollisions << std::endl;
    // Checkstyle: resume
    return false;
}
