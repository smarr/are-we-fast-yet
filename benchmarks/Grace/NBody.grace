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

type NBodyBenchmark = interface {
  expectedEnergy
  innerBenchmarkLoop(innerIterations)
}

type NBodySystem = interface {
  bodies
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
  jupiter
  saturn
  uranus
  neptune
  sun
}

class NBodyBenchmark {
  var expectedEnergy: Number
    
  method innerBenchmarkLoop (innerIterations: Number) -> Boolean {
    var bodies: NBodySystem := NBodySystem

    1.asInteger.to(innerIterations) do { i ->
        bodies.advance(0.01)
    }

    (innerIterations == 250000).ifTrue {
      return bodies.energy == -0.1690859889909308
    }
    
    expectedEnergy.isNil.ifTrue {
      expectedEnergy := bodies.energy
      return true
    }

    return expectedEnergy == bodies.energy
  }
}

class NBodySystem {

  var bodies: List := createBodies

  method createBodies -> List {
    var px: Number := 0
    var py: Number := 0
    var pz: Number := 0

    var bodies: List := [ Body.sun, Body.jupiter, Body.saturn, Body.uranus, Body.neptune ]
    bodies.do { b ->
      px := px + (b.vx * b.mass)
      py := py + (b.vy * b.mass)
      pz := pz + (b.vz * b.mass)
    }

    bodies.at(1.asInteger).offsetMomentumX (px) y (py) z (pz)
    bodies
  }

  method advance (dt) -> Done {

    1.asInteger.to (bodies.size) do { i ->
      var iBody: Body := bodies.at(i)

      (i + 1.asInteger).to (bodies.size) do { j ->

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

    bodies.do { body ->
      body.x := body.x + (dt * body.vx)
      body.y := body.y + (dt * body.vy)
      body.z := body.z + (dt * body.vz)
    }

    Done
  }

  method energy -> Number {
    var dx: Number
    var dy: Number
    var dz: Number
    var distance: Number
    var e: Number := 0.0

    1.asInteger.to (bodies.size) do { i ->
      var iBody: Body := bodies.at(i)

      e := e + (0.5 * iBody.mass * ((iBody.vx * iBody.vx) +
                                    (iBody.vy * iBody.vy) +
                                    (iBody.vz * iBody.vz)))

      (i + 1.asInteger).to (bodies.size) do { j ->
        var jBody: Body := bodies.at(j)
        dx := iBody.x - jBody.x
        dy := iBody.y - jBody.y
        dz := iBody.z - jBody.z

        distance := ((dx * dx) + (dy * dy) + (dz * dz)).sqrt
        e := e - ((iBody.mass * jBody.mass) / distance)
      }
    }
    e
  }
}

def Pi          = 3.141592653589793
def SolarMass   = 4 * Pi * Pi
def DaysPerYear = 365.24

class Body {

  var x    := 0
  var y    := 0
  var z    := 0
  var vx   := 0
  var vy   := 0
  var vz   := 0
  var mass := 0

  method offsetMomentumX (px) y (py) z (pz) {
    vx := 0 - (px / SolarMass)
    vy := 0 - (py / SolarMass)
    vz := 0 - (pz / SolarMass)
  }

  method jupiter {
    var b: Body := Body
    b.x    :=  4.8414314424647209
    b.y    := -1.16032004402742839
    b.z    := -0.103622044471123109
    b.vx   :=  0.00166007664274403694   * DaysPerYear
    b.vy   :=  0.00769901118419740425   * DaysPerYear
    b.vz   := -0.0000690460016972063023 * DaysPerYear
    b.mass :=  0.000954791938424326609  * SolarMass
    return b
  }

  method saturn {
    var b: Body := Body
    b.x    :=  8.34336671824457987
    b.y    :=  4.12479856412430479
    b.z    := -0.403523417114321381
    b.vx   := -0.00276742510726862411   * DaysPerYear
    b.vy   :=  0.00499852801234917238   * DaysPerYear
    b.vz   :=  0.0000230417297573763929 * DaysPerYear
    b.mass :=  0.000285885980666130812  * SolarMass
    return b
  }

  method uranus {
    var b: Body := Body
    b.x    :=  12.894369562139131
    b.y    := -15.1111514016986312
    b.z    := -0.223307578892655734
    b.vx   :=  0.00296460137564761618   * DaysPerYear
    b.vy   :=  0.0023784717395948095    * DaysPerYear
    b.vz   := -0.0000296589568540237556 * DaysPerYear
    b.mass :=  0.0000436624404335156298 * SolarMass
    return b
  }

  method neptune {
    var b: Body := Body
    b.x    :=  15.3796971148509165
    b.y    := -25.9193146099879641
    b.z    :=  0.179258772950371181
    b.vx   :=  0.00268067772490389322   * DaysPerYear
    b.vy   :=  0.00162824170038242295   * DaysPerYear
    b.vz   :=  0.000095159225451971587  * DaysPerYear
    b.mass :=  0.0000515138902046611451 * SolarMass
    return b
  }

  method sun {
    var b := Body
    b.mass := SolarMass
    return b
  }
}


method asString -> String {
  "NBody.grace"
}

method benchmark(innerIterations: Number) {
  var instance: NBodyBenchmark := NBodyBenchmark
  var result: Boolean := instance.innerBenchmarkLoop(innerIterations)                                     
  if (result != true) then {
    error("{self} failed, {result} != 10")
  }
}
