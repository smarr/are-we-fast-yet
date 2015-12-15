'use strict';

var benchmark = require('./benchmark.js'),
  PI = 3.141592653589793,
  SOLAR_MASS = 4 * PI * PI,
  DAYS_PER_YER = 365.24;

function NBody() {
  benchmark.Benchmark.call(this);

  function verifyResult(result, innerIterations) {
    if (innerIterations === 250000) {
      return result === -0.1690859889909308;
    }

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
  }
}

function NBodySystem () {
  this.bodies = this.createBodies();
}

NBodySystem.prototype.createBodies = function () {
  var bodies = new Array(5);
  bodies[0] = Body.sun();
  bodies[1] = Body.jupiter();
  bodies[2] = Body.saturn();
  bodies[3] = Body.uranus();
  bodies[4] = Body.neptune();

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
    e += 0.5 * iBody.mass * (iBody.vx * iBody.vx
      + iBody.vy * iBody.vy
      + iBody.vz * iBody.vz);

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

function Body () {
  this.x = 0.0;
  this.y = 0.0;
  this.z = 0.0;
  this.vx = 0.0;
  this.vy = 0.0;
  this.vz = 0.0;
  this.mass = 0.0;
}

Body.prototype.offsetMomentum = function (px, py, pz) {
  this.vx = 0.0 - (px / SOLAR_MASS);
  this.vy = 0.0 - (py / SOLAR_MASS);
  this.vz = 0.0 - (pz / SOLAR_MASS);
};

Body.jupiter = function () {
  var p = new Body();
  p.x    =  4.84143144246472090e+00;
  p.y    = -1.16032004402742839e+00;
  p.z    = -1.03622044471123109e-01;
  p.vx   =  1.66007664274403694e-03 * DAYS_PER_YER;
  p.vy   =  7.69901118419740425e-03 * DAYS_PER_YER;
  p.vz   = -6.90460016972063023e-05 * DAYS_PER_YER;
  p.mass =  9.54791938424326609e-04 * SOLAR_MASS;
  return p;
};

Body.saturn = function () {
  var p = new Body();
  p.x    =  8.34336671824457987e+00;
  p.y    =  4.12479856412430479e+00;
  p.z    = -4.03523417114321381e-01;
  p.vx   = -2.76742510726862411e-03 * DAYS_PER_YER;
  p.vy   =  4.99852801234917238e-03 * DAYS_PER_YER;
  p.vz   =  2.30417297573763929e-05 * DAYS_PER_YER;
  p.mass =  2.85885980666130812e-04 * SOLAR_MASS;
  return p;
};

Body.uranus = function () {
  var p = new Body();
  p.x    = 1.28943695621391310e+01;
  p.y    = -1.51111514016986312e+01;
  p.z    = -2.23307578892655734e-01;
  p.vx   =  2.96460137564761618e-03 * DAYS_PER_YER;
  p.vy   =  2.37847173959480950e-03 * DAYS_PER_YER;
  p.vz   = -2.96589568540237556e-05 * DAYS_PER_YER;
  p.mass =  4.36624404335156298e-05 * SOLAR_MASS;
  return p;
};

Body.neptune = function () {
  var p = new Body();
  p.x    =  1.53796971148509165e+01;
  p.y    = -2.59193146099879641e+01;
  p.z    =  1.79258772950371181e-01;
  p.vx   =  2.68067772490389322e-03 * DAYS_PER_YER;
  p.vy   =  1.62824170038242295e-03 * DAYS_PER_YER;
  p.vz   = -9.51592254519715870e-05 * DAYS_PER_YER;
  p.mass =  5.15138902046611451e-05 * SOLAR_MASS;
  return p;
};

Body.sun = function () {
  var p = new Body();
  p.mass = SOLAR_MASS;
  return p;
};

exports.newInstance = function () {
  return new NBody();
};
