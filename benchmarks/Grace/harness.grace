import "io" as io
import "mirrors" as mirrors

type Benchmark = interface {
  innerBenchmarkLoop(innerIterations)
  benchmark
  verifyResult(result)
}

type Random = interface {
  next
}

type Run = interface {
  runBenchmark
  printTotal
  numIterations(_)
  innerIterations(_)
}

class newBenchmark -> Benchmark {
  method innerBenchmarkLoop(innerIterations: Number) -> Boolean {
    1.asInteger.to(innerIterations)do { i: Number ->
      verifyResult(benchmark).ifFalse {
        return false
      }
    }
    return true
  }

  method benchmark -> Done { error("sub class responsibility") } // is required.
  method verifyResult(result: Done) -> Boolean { error("sub class responsibility") } // is required.
}

class newRandom -> Random {
  var seed: Number := 74755.asInteger

  method next -> Number {
    seed := ((seed * 1309.asInteger) + 13849.asInteger) & 65535.asInteger
    seed
  }
}

// Robert Jenkins 32 bit integer hash function.
class newJenkins(seed': Number) -> Random {

  // Original version, with complete set of conversions.
  method next -> Number {
    seed := ((seed      + 2127912214.asInteger)       + (seed.as32BitUnsignedValue.bitLeftShift (12.asInteger)).as32BitSignedValue).as32BitSignedValue
    seed := ((seed.bitXor(3345072700.asInteger)).bitXor((seed.as32BitUnsignedValue.bitRightShift(19.asInteger))).as32BitSignedValue)
    seed := ((seed      +  374761393.asInteger)       + (seed.as32BitUnsignedValue.bitLeftShift  (5.asInteger)).as32BitSignedValue).as32BitSignedValue
    seed := ((seed      + 3550635116.asInteger ).bitXor((seed.as32BitUnsignedValue.bitLeftShift  (9.asInteger)).as32BitSignedValue).as32BitSignedValue)
    seed := ((seed      + 4251993797.asInteger)       + (seed.as32BitUnsignedValue.bitLeftShift  (3.asInteger)).as32BitSignedValue).as32BitSignedValue
    seed := ((seed.bitXor(3042594569.asInteger)).bitXor((seed.as32BitUnsignedValue.bitRightShift(16.asInteger))).as32BitSignedValue)
    seed
  }
}

class newRun(name: String) -> Run {
  def benchmarkSuite: Done = io.importModuleByName(name)

  var total: Number := 0.asInteger
  var numIterations: Number := 1.asInteger
  var innerIterations: Number := 1.asInteger

  method runBenchmark -> Done {
    print("Start {name} benchmark ... ")

    doRuns(benchmarkSuite.newInstance)
    reportBenchmark

    print("")
  }

  method measure(bench: Benchmark) -> Done {
    def startTime: Number = platform.system.ticks
    bench.innerBenchmarkLoop(innerIterations).ifFalse {
      error("Benchmark failed with incorrect result")
    }
    def endTime: Number = platform.system.ticks

    def runTime: Number = endTime - startTime
    printResult(runTime)

    total := total + runTime
  }

  method doRuns(bench: Benchmark) -> Done {
    1.asInteger.to(numIterations) do { i: Number ->
      measure(bench)
    }
  }

  method reportBenchmark -> Done {
    print("{name}: iterations={numIterations} average: {total / numIterations}us total: {total}us")
  }

  method printResult(runTime: Number) -> Done {
    print("{name}: iterations=1 runtime: {runTime}us")
  }

  method printTotal -> Done {
    print("\n\nTotal Runtime:{total}us")
  }
}

method processArguments(args: Done) -> Run {
  def run: Run = newRun(args.at(2.asInteger))

  (args.size > 2).ifTrue {
    run.numIterations(args.at(3.asInteger).asInteger)
    (args.size > 3).ifTrue {
      run.innerIterations(args.at(4.asInteger).asInteger)
    }
  }
  run
}

method printUsage -> Done {
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

def run: Run = processArguments(args)

run.runBenchmark
run.printTotal
