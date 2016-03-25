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

var benchmark = require('./benchmark.js'),
  som = require('./som.js');

function Storage() {
  benchmark.Benchmark.call(this);
  this.count = 0;
}
Storage.prototype = Object.create(benchmark.Benchmark.prototype);

Storage.prototype.benchmark = function () {
  var random = new som.Random();
  this.count  = 0;
  this.buildTreeDepth(7, random);
  return this.count;
};

Storage.prototype.verifyResult = function (result) {
  return 5461 === result;
};

Storage.prototype.buildTreeDepth = function (depth, random) {
  this.count += 1;
  if (depth === 1) {
    return new Array(random.next() % 10 + 1);
  } else {
    var arr = new Array(4);
    for (var i = 0; i < 4; i++) {
      arr[i] = this.buildTreeDepth(depth - 1, random);
    }
    return arr;
  }
};

exports.newInstance = function () {
  return new Storage();
};
