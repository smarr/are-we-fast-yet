// @ts-check
// This code is derived from the SOM benchmarks, see AUTHORS.md file.
//
// Copyright (c) 2015-2022 Stefan Marr <git@stefan-marr.de>
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

const benchmarksPreLoaded = {
  Bounce: require('./bounce'),
  CD: require('./cd'),
  DeltaBlue: require('./deltablue'),
  Havlak: require('./havlak'),
  Json: require('./json'),
  List: require('./list'),
  Mandelbrot: require('./mandelbrot'),
  NBody: require('./nbody'),
  Permute: require('./permute'),
  Queens: require('./queens'),
  Richards: require('./richards'),
  Sieve: require('./sieve'),
  Storage: require('./storage'),
  Towers: require('./towers')
};

class Run {
  constructor(name) {
    this.name = name;
    this.benchmarkSuite = this.loadBenchmark();
    this.numIterations = 1;
    this.innerIterations = 1;
    this.total = 0;
  }

  loadBenchmark() {
    return benchmarksPreLoaded[this.name];
  }

  reportBenchmark() {
    process.stdout.write(`${this.name}: iterations=${this.numIterations} average: ${Math.round(this.total / this.numIterations)}us total: ${Math.round(this.total)}us\n\n`);
  }

  printResult(runTime) {
    process.stdout.write(`${this.name}: iterations=1 runtime: ${Math.round(runTime)}us\n`);
  }

  measure(bench) {
    const startTime = process.hrtime();
    if (!bench.innerBenchmarkLoop(this.innerIterations)) {
      throw Error('Benchmark failed with incorrect result');
    }
    const diff = process.hrtime(startTime);
    const runTime = ((diff[0] * 1e9 + diff[1]) / 1000) | 0; // truncate to integer

    this.printResult(runTime);
    this.total += runTime;
  }

  doRuns(bench) {
    for (let i = 0; i < this.numIterations; i += 1) {
      this.measure(bench);
    }
  }

  printTotal() {
    process.stdout.write(`Total Runtime: ${this.total}us\n`);
  }

  runBenchmark() {
    process.stdout.write(`Starting ${this.name} benchmark ...\n`);

    this.doRuns(this.benchmarkSuite.newInstance());

    this.reportBenchmark();
    process.stdout.write('\n');
  }
}

exports.Run = Run;
