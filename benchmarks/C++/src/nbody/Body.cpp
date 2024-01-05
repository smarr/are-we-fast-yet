#include "Body.h"

namespace nbody {
        
    double Body::getX() { return _x; }
    double Body::getY() { return _y; }
    double Body::getZ() { return _z; }

    double Body::getVX() { return _vx; }
    double Body::getVY() { return _vy; }
    double Body::getVZ() { return _vz; }

    double Body::getMass() { return _mass; }

    void Body::setX(double x) { _x = x; }
    void Body::setY(double y) { _y = y; }
    void Body::setZ(double z) { _z = z; }

    void Body::setVX(double vx) { _vx = vx; }
    void Body::setVY(double vy) { _vy = vy; }
    void Body::setVZ(double vz) { _vz = vz; }

    void Body::offsetMomentum(double px, double py, double pz) {
        _vx = 0.0 - (px / SOLAR_MASS);
        _vy = 0.0 - (py / SOLAR_MASS);
        _vz = 0.0 - (pz / SOLAR_MASS);
    }

    Body::Body(double x, double y, double z,
        double vx, double vy, double vz, double mass) {
        _x = x;
        _y = y;
        _z = z;
        _vx = vx * DAYS_PER_YER;
        _vy = vy * DAYS_PER_YER;
        _vz = vz * DAYS_PER_YER;
        _mass = mass * SOLAR_MASS;
    }

    shared_ptr<Body> Body::jupiter() {
        return make_shared<Body>(
            4.84143144246472090e+00,
            -1.16032004402742839e+00,
            -1.03622044471123109e-01,
            1.66007664274403694e-03,
            7.69901118419740425e-03,
            -6.90460016972063023e-05,
            9.54791938424326609e-04);
    }

    shared_ptr<Body> Body::saturn() {
        return make_shared<Body>(
            8.34336671824457987e+00,
            4.12479856412430479e+00,
            -4.03523417114321381e-01,
            -2.76742510726862411e-03,
            4.99852801234917238e-03,
            2.30417297573763929e-05,
            2.85885980666130812e-04);
    }

    shared_ptr<Body> Body::uranus() {
        return make_shared<Body>(
            1.28943695621391310e+01,
            -1.51111514016986312e+01,
            -2.23307578892655734e-01,
            2.96460137564761618e-03,
            2.37847173959480950e-03,
            -2.96589568540237556e-05,
            4.36624404335156298e-05);
    }

    shared_ptr<Body> Body::neptune() {
        return make_shared<Body>(
            1.53796971148509165e+01,
            -2.59193146099879641e+01,
            1.79258772950371181e-01,
            2.68067772490389322e-03,
            1.62824170038242295e-03,
            -9.51592254519715870e-05,
            5.15138902046611451e-05);
    }

    shared_ptr<Body> Body::sun() {
        return make_shared<Body>(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
    }
}
