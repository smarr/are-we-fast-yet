#pragma once

#include <any>

#include "benchmark.h"
#include "som/error.h"

using std::cout;

class Body {
 private:
  constexpr static double PI = 3.141592653589793;
  constexpr static double SOLAR_MASS = 4 * PI * PI;
  constexpr static double DAYS_PER_YER = 365.24;

  double _x;
  double _y;
  double _z;
  double _vx;
  double _vy;
  double _vz;
  double _mass;

 public:
  [[nodiscard]] double getX() const { return _x; }
  [[nodiscard]] double getY() const { return _y; }
  [[nodiscard]] double getZ() const { return _z; }

  [[nodiscard]] double getVX() const { return _vx; }
  [[nodiscard]] double getVY() const { return _vy; }
  [[nodiscard]] double getVZ() const { return _vz; }

  [[nodiscard]] double getMass() const { return _mass; }

  void setX(double x) { _x = x; }
  void setY(double y) { _y = y; }
  void setZ(double z) { _z = z; }

  void setVX(double vx) { _vx = vx; }
  void setVY(double vy) { _vy = vy; }
  void setVZ(double vz) { _vz = vz; }

  void offsetMomentum(double px, double py, double pz) {
    _vx = 0.0 - (px / SOLAR_MASS);
    _vy = 0.0 - (py / SOLAR_MASS);
    _vz = 0.0 - (pz / SOLAR_MASS);
  }

  Body(Body& other) noexcept = default;

  Body(double x,
       double y,
       double z,
       double vx,
       double vy,
       double vz,
       double mass)
      : _x(x),
        _y(y),
        _z(z),
        _vx(vx * DAYS_PER_YER),
        _vy(vy * DAYS_PER_YER),
        _vz(vz * DAYS_PER_YER),
        _mass(mass * SOLAR_MASS) {}

  static Body jupiter() {
    return {4.84143144246472090e+00,  -1.16032004402742839e+00,
            -1.03622044471123109e-01, 1.66007664274403694e-03,
            7.69901118419740425e-03,  -6.90460016972063023e-05,
            9.54791938424326609e-04};
  }
  static Body saturn() {
    return {8.34336671824457987e+00,  4.12479856412430479e+00,
            -4.03523417114321381e-01, -2.76742510726862411e-03,
            4.99852801234917238e-03,  2.30417297573763929e-05,
            2.85885980666130812e-04};
  }
  static Body uranus() {
    return {1.28943695621391310e+01,  -1.51111514016986312e+01,
            -2.23307578892655734e-01, 2.96460137564761618e-03,
            2.37847173959480950e-03,  -2.96589568540237556e-05,
            4.36624404335156298e-05};
  }

  static Body neptune() {
    return {1.53796971148509165e+01, -2.59193146099879641e+01,
            1.79258772950371181e-01, 2.68067772490389322e-03,
            1.62824170038242295e-03, -9.51592254519715870e-05,
            5.15138902046611451e-05};
  }
  static Body sun() { return {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0}; }
};

class NBodySystem {
 private:
  int32_t _bodiesSize{5};
  std::array<Body, 5> _bodies;

 public:
  NBodySystem() : _bodies(createBodies()) {}

  [[nodiscard]] std::array<Body, 5> createBodies() const {
    std::array<Body, 5> bodies = {Body::sun(), Body::jupiter(), Body::saturn(),
                                  Body::uranus(), Body::neptune()};

    double px = 0.0;
    double py = 0.0;
    double pz = 0.0;

    for (int32_t i = 0; i < _bodiesSize; i += 1) {
      px += bodies.at(i).getVX() * bodies.at(i).getMass();
      py += bodies.at(i).getVY() * bodies.at(i).getMass();
      pz += bodies.at(i).getVZ() * bodies.at(i).getMass();
    }

    bodies.at(0).offsetMomentum(px, py, pz);

    return bodies;
  }
  void advance(double dt) {
    for (int32_t i = 0; i < _bodiesSize; i += 1) {
      Body& iBody = _bodies.at(i);

      for (int32_t j = i + 1; j < _bodiesSize; j += 1) {
        Body& jBody = _bodies.at(j);
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

    for (int32_t i = 0; i < _bodiesSize; i += 1) {
      _bodies.at(i).setX(_bodies.at(i).getX() + dt * _bodies.at(i).getVX());
      _bodies.at(i).setY(_bodies.at(i).getY() + dt * _bodies.at(i).getVY());
      _bodies.at(i).setZ(_bodies.at(i).getZ() + dt * _bodies.at(i).getVZ());
    }
  }

  double energy() {
    double e = 0.0;

    for (int32_t i = 0; i < _bodiesSize; i += 1) {
      const Body& iBody = _bodies.at(i);
      e += 0.5 * iBody.getMass() *
           (iBody.getVX() * iBody.getVX() + iBody.getVY() * iBody.getVY() +
            iBody.getVZ() * iBody.getVZ());

      for (int32_t j = i + 1; j < _bodiesSize; j += 1) {
        const Body& jBody = _bodies.at(j);
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

class NBody : public Benchmark {
 private:
  bool verify_result(double result, int32_t innerIterations) {
    if (innerIterations == 250000) {
      return result == -0.1690859889909308;
    }
    if (innerIterations == 1) {
      return result == -0.16907495402506745;
    }

    cout << "No verification result for " << innerIterations << " found\n";
    cout << "Result is: " << result << "\n";
    return false;
  }

 public:
  bool inner_benchmark_loop(int32_t innerIterations) override {
    NBodySystem system{};
    for (int32_t i = 0; i < innerIterations; i += 1) {
      system.advance(0.01);
    }

    return verify_result(system.energy(), innerIterations);
  }

  std::any benchmark() override { throw Error("Should never be reached"); }
  bool verify_result(std::any) override {
    throw Error("Should never be reached");
  }
};
