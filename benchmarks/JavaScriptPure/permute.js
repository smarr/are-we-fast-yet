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

function Permute() {
  Benchmark.call(this);
  this.count = 0;
  this.v     = null;
}
Permute.prototype = Object.create(Benchmark.prototype);

Permute.prototype.benchmark = function () {
  this.count = 0;
  this.v     = new Array(6).fill(0);
  this.permute(6);
  return this.count;
};

Permute.prototype.verifyResult = function (result) {
  return result === 8660;
};

Permute.prototype.permute = function (n) {
  this.count += 1;
  if (n !== 0) {
    var n1 = n - 1;
    this.permute(n1);
    for (var i = n1; i >= 0; i--) {
      this.swap(n1, i);
      this.permute(n1);
      this.swap(n1, i);
    }
  }
};

Permute.prototype.swap = function (i, j) {
  var tmp = this.v[i];
  this.v[i] = this.v[j];
  this.v[j] = tmp;
};

var run = new Run(Permute, "Permute", 100, 1000);
run.runBenchmark();
run.printTotal();

