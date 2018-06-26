import "io" as io
import "mirrors" as mirrors

class Benchmark {
  method innerBenchmarkLoop(innerIterations) {
    1.asInteger.to(innerIterations)do { i ->
      verifyResult(benchmark).ifFalse {
        return false
      }
    }
    return true
  }

  method benchmark { error("sub class responsibility") } // is required.
  method verifyResult(result) { error("sub class responsibility") } // is required.
}

class Random {
  var seed := 74755.asInteger

  method next {
    seed := ((seed * 1309.asInteger) + 13849.asInteger) & 65535.asInteger
    seed
  }
}

// Robert Jenkins 32 bit integer hash function.
class Jenkins(seed') {

  // Original version, with complete set of conversions.
  method next {
    seed := ((seed      + 2127912214.asInteger)       + (seed.as32BitUnsignedValue.bitLeftShift (12.asInteger)).as32BitSignedValue).as32BitSignedValue
    seed := ((seed.bitXor(3345072700.asInteger)).bitXor((seed.as32BitUnsignedValue.bitRightShift(19.asInteger))).as32BitSignedValue)
    seed := ((seed      +  374761393.asInteger)       + (seed.as32BitUnsignedValue.bitLeftShift  (5.asInteger)).as32BitSignedValue).as32BitSignedValue
    seed := ((seed      + 3550635116.asInteger ).bitXor((seed.as32BitUnsignedValue.bitLeftShift  (9.asInteger)).as32BitSignedValue).as32BitSignedValue)
    seed := ((seed      + 4251993797.asInteger)       + (seed.as32BitUnsignedValue.bitLeftShift  (3.asInteger)).as32BitSignedValue).as32BitSignedValue
    seed := ((seed.bitXor(3042594569.asInteger)).bitXor((seed.as32BitUnsignedValue.bitRightShift(16.asInteger))).as32BitSignedValue)
    seed
  }
}

class Run(name) {
  def benchmarkSuite = io.importModuleByName(name)

  var total := 0.asInteger
  var numIterations   := 1.asInteger
  var innerIterations := 1.asInteger

  method runBenchmark {
    print("Start {name} benchmark ... ")

    doRuns(benchmarkSuite.newInstance)
    reportBenchmark

    print("")
  }

  method measure(bench) {
    def startTime = platform.system.ticks
    bench.innerBenchmarkLoop(innerIterations).ifFalse {
      error("Benchmark failed with incorrect result")
    }
    def endTime = platform.system.ticks

    def runTime = endTime - startTime
    printResult(runTime)

    total := total + runTime
  }

  method doRuns(bench) {
    1.asInteger.to(numIterations) do { i ->
      measure(bench)
    }
  }

  method reportBenchmark {
    print("{name}: iterations={numIterations} average: {total / numIterations}us total: {total}us")
  }

  method printResult(runTime) {
    print("{name}: iterations=1 runtime: {runTime}us")
  }

  method printTotal {
    print("\n\nTotal Runtime:{total}us")
  }
}

method processArguments(args) {
  def run = Run(args.at(2.asInteger))

  (args.size > 2).ifTrue {
    run.numIterations(args.at(3.asInteger).asInteger)
    (args.size > 3).ifTrue {
      run.innerIterations(args.at(4.asInteger).asInteger)
    }
  }
  run
}

method printUsage {
  print("./moth harness.grace benchmark [num-iterations [inner-iter]]")
  print("")
  print("  benchmark      - benchmark class name")
  print("  num-iterations - number of times to execute benchmark, default: 1")
  print("  inner-iter     - number of times the benchmark is executed in an inner loop, ")
  print("                   which is measured in total, default: 1")
}

(args.size < 2).ifTrue {
  printUsage
  return 1.asInteger
}

def run = processArguments(args)

run.runBenchmark
run.printTotal
