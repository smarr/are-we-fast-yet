// This code is derived from the SOM benchmarks, see AUTHORS.md file.
//
// Copyright (c) 2015-2016 Stefan Marr <git@stefan-marr.de>
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the 'Software'), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
'use strict';

function Run(name) {
  function loadBenchmark() {
    var filename = "./" + name.toLowerCase() + ".js";
    return require(filename);
  }

  var benchmarkSuite  = loadBenchmark(),
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
    var runTime = ((diff[0] * 1e9 + diff[1]) / 1000) | 0; // truncate to integer

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
  };
}

function processArguments(args) {
  var run = new Run(args[2]);

  if (args.length > 3) {
    run.setNumIterations(parseInt(args[3]));
    if (args.length > 4) {
      run.setInnerIterations(parseInt(args[4]));
    }
  }
  return run;
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

var run = processArguments(process.argv);
run.runBenchmark();
run.printTotal();
