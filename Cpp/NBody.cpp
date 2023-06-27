/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted based on the SOM version.
 * Copyright (c) 2023 Rochus Keller <me@rochus-keller.ch> (for C++ migration)
 */

#include "NBody.h"
#include <iostream>
#include <math.h>
#include <stdlib.h>

static const double PI = 3.141592653589793;
static const double SOLAR_MASS = 4 * PI * PI;
static const double DAYS_PER_YER = 365.24;


class Body {
    double x;
    double y;
    double z;
    double vx;
    double vy;
    double vz;
    double mass;

public:
    double getX() const { return x; }
    double getY() const { return y; }
    double getZ() const { return z; }

    double getVX() const { return vx; }
    double getVY() const { return vy; }
    double getVZ() const { return vz; }

    double getMass() const { return mass; }

    void setX(double x) { this->x = x; }
    void setY(double y) { this->y = y; }
    void setZ(double z) { this->z = z; }

    void setVX(double vx) { this->vx = vx; }
    void setVY(double vy) { this->vy = vy; }
    void setVZ(double vz) { this->vz = vz; }

    void offsetMomentum(double px, double py, double pz) {
        vx = 0.0 - (px / SOLAR_MASS);
        vy = 0.0 - (py / SOLAR_MASS);
        vz = 0.0 - (pz / SOLAR_MASS);
    }

    Body(double x = 0.0, double y = 0.0, double z = 0.0,
         double vx = 0.0, double vy = 0.0, double vz = 0.0, double mass = 0.0) {
        this->x = x;
        this->y = y;
        this->z = z;
        this->vx = vx * DAYS_PER_YER;
        this->vy = vy * DAYS_PER_YER;
        this->vz = vz * DAYS_PER_YER;
        this->mass = mass * SOLAR_MASS;
    }

    static Body jupiter() {
        return Body(
                    4.84143144246472090e+00,
                    -1.16032004402742839e+00,
                    -1.03622044471123109e-01,
                    1.66007664274403694e-03,
                    7.69901118419740425e-03,
                    -6.90460016972063023e-05,
                    9.54791938424326609e-04);
    }

    static Body saturn() {
        return Body(
                    8.34336671824457987e+00,
                    4.12479856412430479e+00,
                    -4.03523417114321381e-01,
                    -2.76742510726862411e-03,
                    4.99852801234917238e-03,
                    2.30417297573763929e-05,
                    2.85885980666130812e-04);
    }

    static Body uranus() {
        return Body(
                    1.28943695621391310e+01,
                    -1.51111514016986312e+01,
                    -2.23307578892655734e-01,
                    2.96460137564761618e-03,
                    2.37847173959480950e-03,
                    -2.96589568540237556e-05,
                    4.36624404335156298e-05);
    }

    static Body neptune() {
        return Body(
                    1.53796971148509165e+01,
                    -2.59193146099879641e+01,
                    1.79258772950371181e-01,
                    2.68067772490389322e-03,
                    1.62824170038242295e-03,
                    -9.51592254519715870e-05,
                    5.15138902046611451e-05);
    }

    static Body sun() {
        return Body(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
    }
};

class NBodySystem {
    enum { Count = 5 };
    Body bodies[Count];

public:
    NBodySystem() {
        bodies[0] = Body::sun();
        bodies[1] = Body::jupiter();
        bodies[2] = Body::saturn();
        bodies[3] = Body::uranus();
        bodies[4] = Body::neptune();

        double px = 0.0;
        double py = 0.0;
        double pz = 0.0;

        for ( int i = 0; i < Count; i++ ) {
            const Body& b = bodies[i];
            px += b.getVX() * b.getMass();
            py += b.getVY() * b.getMass();
            pz += b.getVZ() * b.getMass();
        }

        bodies[0].offsetMomentum(px, py, pz);
    }

    void advance(double dt) {

        for (int i = 0; i < Count; ++i) {
            Body& iBody = bodies[i];

            for (int j = i + 1; j < Count; ++j) {
                Body& jBody = bodies[j];
                const double dx = iBody.getX() - jBody.getX();
                const double dy = iBody.getY() - jBody.getY();
                const double dz = iBody.getZ() - jBody.getZ();

                const double dSquared = dx * dx + dy * dy + dz * dz;
                const double distance = sqrt(dSquared);
                const double mag = dt / (dSquared * distance);

                iBody.setVX(iBody.getVX() - (dx * jBody.getMass() * mag));
                iBody.setVY(iBody.getVY() - (dy * jBody.getMass() * mag));
                iBody.setVZ(iBody.getVZ() - (dz * jBody.getMass() * mag));

                jBody.setVX(jBody.getVX() + (dx * iBody.getMass() * mag));
                jBody.setVY(jBody.getVY() + (dy * iBody.getMass() * mag));
                jBody.setVZ(jBody.getVZ() + (dz * iBody.getMass() * mag));
            }
        }

        for (int i = 0; i < Count; i++) {
            Body& body = bodies[i];
            body.setX(body.getX() + dt * body.getVX());
            body.setY(body.getY() + dt * body.getVY());
            body.setZ(body.getZ() + dt * body.getVZ());
        }
    }

    double energy() {
        double e = 0.0;

        for (int i = 0; i < Count; ++i) {
            const Body& iBody = bodies[i];
            e += 0.5 * iBody.getMass()
                    * (iBody.getVX() * iBody.getVX() +
                       iBody.getVY() * iBody.getVY() +
                       iBody.getVZ() * iBody.getVZ());

            for (int j = i + 1; j < Count; ++j) {
                const Body& jBody = bodies[j];
                const double dx = iBody.getX() - jBody.getX();
                const double dy = iBody.getY() - jBody.getY();
                const double dz = iBody.getZ() - jBody.getZ();

                const double distance = sqrt(dx * dx + dy * dy + dz * dz);
                e -= (iBody.getMass() * jBody.getMass()) / distance;
            }
        }
        return e;
    }
};


bool NBody::innerBenchmarkLoop(int innerIterations)
{
    NBodySystem system;
    for (int i = 0; i < innerIterations; i++) {
        system.advance(0.01);
    }

    return verifyResult(system.energy(), innerIterations);
}

bool NBody::verifyResult(double result, int innerIterations)
{
    const double epsilon = 0.00000000000000005; // 5e-17
    if (innerIterations == 250000) {
        return abs(result) - 0.1690859889909308 < epsilon;
    }
    if (innerIterations == 1) {
        return abs(result) - 0.16907495402506745 < epsilon;
    }

    // Checkstyle: stop
    std::cerr << "No verification result for " << innerIterations << " found" << std::endl;
    std::cerr << "Result is: " << result << std::endl;
    // Checkstyle: resume
    return false;
}
