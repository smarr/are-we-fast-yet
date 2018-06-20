import "io" as io
import "mirrors" as mirrors

var module
var suite
var innerIterations
var warumpIterations
var outerIterations

method getSuiteByName(name) {
  io.importModuleByName(name)
}

method doIterations {
  mirrors.invoke ("benchmark:") on (suite) withArguments ( [ innerIterations ] )
}


method runBenchmark {
  print("Start {suite} benchmark ... ")

  def times = platform.kernel.Vector.new
  var start
  var end
  var time
  1.asInteger.to(outerIterations) do { i ->
    start := platform.system.ticks
    doIterations
    end := platform.system.ticks
    time := end - start

    (i > warumpIterations).ifTrue {
      times.append(time)  
    }

    print("{suite}: iterations={1.asInteger} runtime: {time}us")
  }

  // Print out human-readable information
  var total := 0.asInteger
  times.do { time -> total := total + time }
  var average := (total / times.size)
  print("{suite}: iterations={outerIterations} average: {average}us total: {total}us")
  print("\n\nTotal Runtime:{total}us")
}

method runBenchmarks(suite) {
  var names := findBenchmarksInSuite(suite)

  prettyPrint("Running benchmarks in " + suite.asString)
  names.do { name ->
    runBenchmark(suite, name)
  }
}

method invoke (moduleName) with (nOuter, nWarmup, nInner) {
    module := moduleName
    suite := getSuiteByName(module)
    outerIterations := nOuter
    warumpIterations := nWarmup
    innerIterations := nInner
    runBenchmark
}


method exe(args) {
  // Run each benchmark once
  (args.size == 1).ifTrue {
    invoke ( "GraceLibrary/Benchmarks/Bounce.grace"   ) with ( 100.asInteger, 30.asInteger, 1000.asInteger )
    invoke ( "GraceLibrary/Benchmarks/Fannkuch.grace" ) with ( 100.asInteger, 30.asInteger,    9.asInteger )
    invoke ( "GraceLibrary/Benchmarks/List.grace"     ) with ( 100.asInteger, 30.asInteger, 1000.asInteger )
    invoke ( "GraceLibrary/Benchmarks/Permute.grace"  ) with ( 100.asInteger, 30.asInteger,  500.asInteger )
    invoke ( "GraceLibrary/Benchmarks/Queens.grace"   ) with ( 100.asInteger, 30.asInteger,  400.asInteger )
    invoke ( "GraceLibrary/Benchmarks/Sieve.grace"    ) with ( 100.asInteger, 30.asInteger,  400.asInteger )
    invoke ( "GraceLibrary/Benchmarks/Towers.grace"   ) with ( 100.asInteger, 30.asInteger,  300.asInteger )
    return 0
  }

  // Run the given benchmark
  (args.size == 5).ifTrue {
    invoke (args.at(2.asInteger)) with (args.at(3.asInteger).asInteger, args.at(4.asInteger).asInteger, args.at(5.asInteger).asInteger)
    return 0
  }

  print("Must run the harness by itself (with no argument) or on a particular benchmark with three additional arguments:")
  print("> ./moth GraceLibrary/Benchmarks/harness.grace")
  print("> ./moth GraceLibrary/Benchmarks/harness.grace <module> <outer> <unused> <inner>")
}

exe(args)