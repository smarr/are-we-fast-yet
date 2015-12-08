'use strict';

function Run() {
  var name            = null,
      benchmarkSuite  = null,
      numIterations   = 1,
      innerIterations = 1,
      total           = 0;

  function reportBenchmark() {
    process.stdout.write(name + ": iterations=" + numIterations +
      " average: " + Math.round(total / numIterations) + "us total: " + Math.round(total) + "us\n\n");
  }

  function printResult(runTime) {
    process.stdout.write(name + ": iterations=1 runtime: " + Math.round(runTime) + "us\n");
  }

  function measure(bench) {
    var startTime =  process.hrtime();
    if (!bench.innerBenchmarkLoop(innerIterations)) {
      throw "Benchmark failed with incorrect result";
    }
    var diff =  process.hrtime(startTime);
    var runTime = (diff[0] * 1e9 + diff[1]) / 1000;

    printResult(runTime);
    total += runTime;
  }

  function doRuns(bench) {
    for (var i = 0; i < numIterations; i++) {
      measure(bench);
    }
  }

  this.printTotal = function () {
    process.stdout.write("Total Runtime: " + total + "us\n");
  };

  this.runBenchmark = function () {
    process.stdout.write("Starting " + name + " benchmark ...\n");

    doRuns(benchmarkSuite.newInstance());

    reportBenchmark();
    process.stdout.write("\n");
  };

  this.setNumIterations = function (val) {
    numIterations = val;
  };

  this.setName = function (val) {
    name = val;
  };

  this.setInnerIterations = function (val) {
    innerIterations = val;
  };

  this.setBenchmarkSuite = function (val) {
    benchmarkSuite = val;
  }
}

function loadBenchmark(name) {
  var filename = "./" + name.toLowerCase() + ".js";
  var benchSuite = require(filename);
  return benchSuite;
}

function processArguments(args, run) {
  run.setName(args[2]);
  run.setBenchmarkSuite(loadBenchmark(args[2]));

  if (args.length > 3) {
    run.setNumIterations(parseInt(args[3]));
    if (args.length > 4) {
      run.setInnerIterations(parseInt(args[4]));
    }
  }
}

function printUsage() {
  process.stdout.write("harness.js [benchmark] [num-iterations [inner-iter]]\n");
  process.stdout.write("\n");
  process.stdout.write("  benchmark      - benchmark class name\n");
  process.stdout.write("  num-iterations - number of times to execute benchmark, default: 1\n");
  process.stdout.write("  inner-iter     - number of times the benchmark is executed in an inner loop,\n");
  process.stdout.write("                   which is measured in total, default: 1\n");
}

if (process.argv.length < 3) {
  printUsage();
  process.exit(1);
}

var run = new Run();
processArguments(process.argv, run);
run.runBenchmark();
run.printTotal();
