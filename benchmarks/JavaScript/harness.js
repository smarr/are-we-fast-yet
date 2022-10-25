// @ts-check
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

const { Run } = require('./run');

function processArguments(args) {
  const run = new Run(args[2]);

  if (args.length > 3) {
    run.numIterations = parseInt(args[3], 10);
    if (args.length > 4) {
      run.innerIterations = parseInt(args[4], 10);
    }
  }
  return run;
}

function printUsage() {
  process.stdout.write('harness.js [benchmark] [num-iterations [inner-iter]]\n');
  process.stdout.write('\n');
  process.stdout.write('  benchmark      - benchmark class name\n');
  process.stdout.write('  num-iterations - number of times to execute benchmark, default: 1\n');
  process.stdout.write('  inner-iter     - number of times the benchmark is executed in an inner loop,\n');
  process.stdout.write('                   which is measured in total, default: 1\n');
}

if (process.argv.length < 3) {
  printUsage();
  process.exit(1);
}

const run = processArguments(process.argv);
run.runBenchmark();
run.printTotal();
