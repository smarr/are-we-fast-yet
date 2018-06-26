//
// The Computer Language Benchmarks Game
// http://shootout.alioth.debian.org/
//
// contributed by Mark C. Lewis
// modified slightly by Chad Whipkey
//
//  Based on nbody.java ported to SOM by Stefan Marr.
//
//
// Adapted for Grace by Richard Roberts
//   2018, June
//

import "harness" as harness

type NBodySystem = interface {
  createBodies
  advance(dt)
  energy
}

type Body = interface {
  x
  y
  z
  vx
  vy
  vz
  offsetMomentumX (px) y (py) z (pz)
  mass
}

def Pi: Number          = 3.141592653589793
def DaysPerYear: Number = 365.24
def SolarMass: Number   = 4.0 * Pi * Pi


class newNBody -> Benchmark {
  inherit harness.newBenchmark

  method innerBenchmarkLoop (innerIterations: Number) -> Boolean {
    var system: NBodySystem := newNBodySystem

    1.asInteger.to(innerIterations) do { i: Number ->
      system.advance(0.01)
    }

    return verify(system.energy)for(innerIterations)
  }

  method verify(result: Number)for(innerIterations: Number) -> Boolean {
    (innerIterations == 250000).ifTrue { return result == -0.1690859889909308 }
    (innerIterations ==      1).ifTrue { return result == -0.16907495402506745 }

    print("No verification result for {innerIterations} found")
    print("Result is: {result}")
    return false
  }
}

class newNBodySystem -> NBodySystem {
  var bodies: List := createBodies

  method createBodies -> List {
    var bodies: List := [ sun, jupiter, saturn, uranus, neptune ]

    var px: Number := 0.0
    var py: Number := 0.0
    var pz: Number := 0.0

    bodies.do { b: Body ->
      px := px + (b.vx * b.mass)
      py := py + (b.vy * b.mass)
      pz := pz + (b.vz * b.mass)
    }

    bodies.at(1.asInteger).offsetMomentumX (px) y (py) z (pz)
    bodies
  }

  method advance (dt: Number) -> Done {
    1.asInteger.to (bodies.size) do { i: Number ->
      var iBody: Body := bodies.at(i)

      (i + 1.asInteger).to (bodies.size) do { j: Number ->
        var jBody: Body := bodies.at(j)
        var dx: Number := iBody.x - jBody.x
        var dy: Number := iBody.y - jBody.y
        var dz: Number := iBody.z - jBody.z

        var dSquared: Number := (dx * dx) + (dy * dy) + (dz * dz)
        var distance: Number := dSquared.sqrt
        var mag: Number := dt / (dSquared * distance)

        iBody.vx := iBody.vx - (dx * jBody.mass * mag)
        iBody.vy := iBody.vy - (dy * jBody.mass * mag)
        iBody.vz := iBody.vz - (dz * jBody.mass * mag)

        jBody.vx := jBody.vx + (dx * iBody.mass * mag)
        jBody.vy := jBody.vy + (dy * iBody.mass * mag)
        jBody.vz := jBody.vz + (dz * iBody.mass * mag)
      }
    }

    bodies.do { body: Body ->
      body.x := body.x + (dt * body.vx)
      body.y := body.y + (dt * body.vy)
      body.z := body.z + (dt * body.vz)
    }
  }

  method energy -> Number {
    var e: Number := 0.0

    1.asInteger.to (bodies.size) do { i: Number ->
      var iBody: Body := bodies.at(i)

      e := e + (0.5 * iBody.mass * ((iBody.vx * iBody.vx) +
                                    (iBody.vy * iBody.vy) +
                                    (iBody.vz * iBody.vz)))

      (i + 1.asInteger).to (bodies.size) do { j: Number ->
        var jBody: Body := bodies.at(j)
        var dx: Number := iBody.x - jBody.x
        var dy: Number := iBody.y - jBody.y
        var dz: Number := iBody.z - jBody.z

        var distance: Number := ((dx * dx) + (dy * dy) + (dz * dz)).sqrt
        e := e - ((iBody.mass * jBody.mass) / distance)
      }
    }
    e
  }
}

class newBody -> Body {
  var x: Number    := 0.0
  var y: Number    := 0.0
  var z: Number    := 0.0
  var vx: Number   := 0.0
  var vy: Number   := 0.0
  var vz: Number   := 0.0
  var mass: Number := 0.0

  method offsetMomentumX (px: Number) y (py: Number) z (pz: Number) -> Done {
    vx := 0.0 - (px / SolarMass)
    vy := 0.0 - (py / SolarMass)
    vz := 0.0 - (pz / SolarMass)
  }
}

method jupiter -> Body {
  var b: Body := newBody
  b.x    :=  4.8414314424647209
  b.y    := -1.16032004402742839
  b.z    := -0.103622044471123109
  b.vx   :=  0.00166007664274403694   * DaysPerYear
  b.vy   :=  0.00769901118419740425   * DaysPerYear
  b.vz   := -0.0000690460016972063023 * DaysPerYear
  b.mass :=  0.000954791938424326609  * SolarMass
  return b
}

method saturn -> Body {
  var b: Body := newBody
  b.x    :=  8.34336671824457987
  b.y    :=  4.12479856412430479
  b.z    := -0.403523417114321381
  b.vx   := -0.00276742510726862411   * DaysPerYear
  b.vy   :=  0.00499852801234917238   * DaysPerYear
  b.vz   :=  0.0000230417297573763929 * DaysPerYear
  b.mass :=  0.000285885980666130812  * SolarMass
  return b
}

method uranus -> Body {
  var b: Body := newBody
  b.x    :=  12.894369562139131
  b.y    := -15.1111514016986312
  b.z    := -0.223307578892655734
  b.vx   :=  0.00296460137564761618   * DaysPerYear
  b.vy   :=  0.0023784717395948095    * DaysPerYear
  b.vz   := -0.0000296589568540237556 * DaysPerYear
  b.mass :=  0.0000436624404335156298 * SolarMass
  return b
}

method neptune -> Body {
  var b: Body := newBody
  b.x    :=  15.3796971148509165
  b.y    := -25.9193146099879641
  b.z    :=  0.179258772950371181
  b.vx   :=  0.00268067772490389322   * DaysPerYear
  b.vy   :=  0.00162824170038242295   * DaysPerYear
  b.vz   := -0.000095159225451971587  * DaysPerYear
  b.mass :=  0.0000515138902046611451 * SolarMass
  return b
}

method sun -> Body {
  var b: Body := newBody
  b.mass := SolarMass
  return b
}

method newInstance -> Benchmark { newNBody }
