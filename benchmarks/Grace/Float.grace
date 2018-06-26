import "harness" as harness

def points: Number = 100000

type Point = interface {
  x
  y
  z
  normalize
  maximize(other)
}

class newPoint(i: Number) -> Point {
  var x: Number := i.sin
  var y: Number := i.cos * 3
  var z: Number := x * x / 2

  method normalize -> Done {
    var x': Number := self.x
    var y': Number := self.y
    var z': Number := self.z
    var norm: Number := (x * x + y * y + z * z).sqrt
    self.x := self.x / norm
    self.y := self.y / norm
    self.z := self.z / norm
  }

  method maximize(other: Point) -> Point {
    x := (x > other.x).ifTrue {x} ifFalse {other.x}
    y := (y > other.y).ifTrue {y} ifFalse {other.y}
    z := (z > other.z).ifTrue {z} ifFalse {other.z}
    return self
  }

  method asString -> String {
    "<Point: x=" + x + " y=" + y + " z=" + z + ">"
  }
}

class newFloat -> Benchmark {
  inherit harness.newBenchmark

  method innerBenchmarkLoop(innerIterations: Number) -> Boolean {
    var points: List := platform.kernel.Array.new(innerIterations)

    1.asInteger.to(innerIterations) do { i: Number ->
      points.at(i)put(newPoint(i - 1))
    }

    points.do { p: Point ->
      p.normalize
    }

    def point: Point = maximize(points)
    return verify(innerIterations)resultFor(point)
  }

  method maximize(points: List) -> Point {
    var next: Point := points.at(1.asInteger)
    2.asInteger.to(points.size) do { i: Number ->
      def p: Point = points.at(i)
      next := next.maximize(p)
    }
    return next
  }

  method verify (innerIterations: Number) resultFor (point: Point) -> Boolean {
    (innerIterations ==      10).ifTrue { return (point.x == 0.8335183971759773) && (point.y == 1.0) && (point.z == 0.41232414997917820) }
    (innerIterations ==     500).ifTrue { return (point.x == 0.8943650417038651) && (point.y == 1.0) && (point.z == 0.44717903597108100) }
    (innerIterations ==    1000).ifTrue { return (point.x == 0.8943675385681149) && (point.y == 1.0) && (point.z == 0.44717950831719694) }
    (innerIterations ==    4000).ifTrue { return (point.x == 0.8943739466731936) && (point.y == 1.0) && (point.z == 0.44718361320870764) }
    (innerIterations ==   10000).ifTrue { return (point.x == 0.8943870342151542) && (point.y == 1.0) && (point.z == 0.44719064873006503) }
    (innerIterations ==  100000).ifTrue { return (point.x == 0.8944271890997864) && (point.y == 1.0) && (point.z == 0.44721359544569720) }
    (innerIterations == 1000000).ifTrue { return (point.x == 0.8944271909996454) && (point.y == 1.0) && (point.z == 0.44721359549980340) }
    print("No verification result for {innerIterations} found (result was {point}).")
    return false
  }
}

method newInstance -> Benchmark { newFloat }
