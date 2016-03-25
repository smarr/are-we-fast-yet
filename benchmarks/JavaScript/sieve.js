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

var benchmark = require('./benchmark.js');

function Sieve() {
  benchmark.Benchmark.call(this);
}
Sieve.prototype = Object.create(benchmark.Benchmark.prototype);

function sieve(flags, size) {
  var primeCount = 0;

  for (var i = 2; i <= size; i++) {
    if (flags[i - 1]) {
      primeCount++;
      var k = i + i;
      while (k <= size) {
        flags[k - 1] = false;
        k += i;
      }
    }
  }
  return primeCount;
}

Sieve.prototype.benchmark = function () {
  var flags = new Array(5000);
  flags.fill(true);
  return sieve(flags, 5000);
};

Sieve.prototype.verifyResult = function (result) {
  return 669 == result;
};

exports.newInstance = function () {
  return new Sieve();
};
