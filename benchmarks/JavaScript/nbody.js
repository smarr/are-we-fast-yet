// The Computer Language Benchmarks Game
// http://shootout.alioth.debian.org/
//
//     contributed by Mark C. Lewis
// modified slightly by Chad Whipkey
//
// Based on nbody.java ported to SOM, and then JavaScript by Stefan Marr.
'use strict';

var benchmark = require('./benchmark.js'),
  PI = 3.141592653589793,
  SOLAR_MASS = 4 * PI * PI,
  DAYS_PER_YER = 365.24;

function NBody() {
  benchmark.Benchmark.call(this);

  function verifyResult(result, innerIterations) {
    if (innerIterations === 250000) { return result === -0.1690859889909308;  }
    if (innerIterations ===      1) { return result === -0.16907495402506745; }

    process.stdout.write("No verification result for " + innerIterations + " found\n");
    process.stdout.write("Result is: " + result + "\n");
    return false;
  }

  this.innerBenchmarkLoop = function (innerIterations) {
    var system = new NBodySystem();
    for (var i = 0; i < innerIterations; i++) {
      system.advance(0.01);
    }

    return verifyResult(system.energy(), innerIterations);
  };
}

function NBodySystem () {
  this.bodies = this.createBodies();
}

NBodySystem.prototype.createBodies = function () {
  var bodies = [Body.sun(),
                Body.jupiter(),
                Body.saturn(),
                Body.uranus(),
                Body.neptune()];

  var px = 0.0,
    py   = 0.0,
    pz   = 0.0;

  bodies.forEach(function (b) {
    px += b.vx * b.mass;
    py += b.vy * b.mass;
    pz += b.vz * b.mass;
  });

  bodies[0].offsetMomentum(px, py, pz);

  return bodies;
};

NBodySystem.prototype.advance = function (dt) {
  for (var i = 0; i < this.bodies.length; ++i) {
    var iBody = this.bodies[i];

    for (var j = i + 1; j < this.bodies.length; ++j) {
      var jBody = this.bodies[j],
        dx = iBody.x - jBody.x,
        dy = iBody.y - jBody.y,
        dz = iBody.z - jBody.z,

        dSquared = dx * dx + dy * dy + dz * dz,
        distance = Math.sqrt(dSquared),
        mag = dt / (dSquared * distance);

      iBody.vx -= dx * jBody.mass * mag;
      iBody.vy -= dy * jBody.mass * mag;
      iBody.vz -= dz * jBody.mass * mag;

      jBody.vx += dx * iBody.mass * mag;
      jBody.vy += dy * iBody.mass * mag;
      jBody.vz += dz * iBody.mass * mag;
    }
  }

  this.bodies.forEach(function (body) {
    body.x += dt * body.vx;
    body.y += dt * body.vy;
    body.z += dt * body.vz;
  });
};

NBodySystem.prototype.energy = function () {
  var dx = 0.0,
    dy = 0.0,
    dz = 0.0,
    distance = 0.0,
    e = 0.0;

  for (var i = 0; i < this.bodies.length; ++i) {
    var iBody = this.bodies[i];
    e += 0.5 * iBody.mass * (iBody.vx * iBody.vx +
      iBody.vy * iBody.vy +
      iBody.vz * iBody.vz);

    for (var j = i + 1; j < this.bodies.length; ++j) {
      var jBody = this.bodies[j];
      dx = iBody.x - jBody.x;
      dy = iBody.y - jBody.y;
      dz = iBody.z - jBody.z;

      distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      e -= (iBody.mass * jBody.mass) / distance;
    }
  }
  return e;
};

function Body (x, y, z, vx, vy, vz, mass) {
  this.x = x;
  this.y = y;
  this.z = z;
  this.vx = vx * DAYS_PER_YER;
  this.vy = vy * DAYS_PER_YER;
  this.vz = vz * DAYS_PER_YER;
  this.mass = mass * SOLAR_MASS;
}

Body.prototype.offsetMomentum = function (px, py, pz) {
  this.vx = 0.0 - (px / SOLAR_MASS);
  this.vy = 0.0 - (py / SOLAR_MASS);
  this.vz = 0.0 - (pz / SOLAR_MASS);
};

Body.jupiter = function () {
  return new Body(4.84143144246472090e+00,
                 -1.16032004402742839e+00,
                 -1.03622044471123109e-01,
                  1.66007664274403694e-03,
                  7.69901118419740425e-03,
                 -6.90460016972063023e-05,
                  9.54791938424326609e-04);
};

Body.saturn = function () {
  return new Body(8.34336671824457987e+00,
                  4.12479856412430479e+00,
                 -4.03523417114321381e-01,
                 -2.76742510726862411e-03,
                  4.99852801234917238e-03,
                  2.30417297573763929e-05,
                  2.85885980666130812e-04);
};

Body.uranus = function () {
  return new Body(1.28943695621391310e+01,
                 -1.51111514016986312e+01,
                 -2.23307578892655734e-01,
                  2.96460137564761618e-03,
                  2.37847173959480950e-03,
                 -2.96589568540237556e-05,
                  4.36624404335156298e-05);
};

Body.neptune = function () {
  return new Body(1.53796971148509165e+01,
                 -2.59193146099879641e+01,
                  1.79258772950371181e-01,
                  2.68067772490389322e-03,
                  1.62824170038242295e-03,
                 -9.51592254519715870e-05,
                  5.15138902046611451e-05);
};

Body.sun = function () {
  return new Body(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
};

exports.newInstance = function () {
  return new NBody();
};
