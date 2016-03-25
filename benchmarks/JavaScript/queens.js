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

function Queens() {
  benchmark.Benchmark.call(this);

  this.freeMaxs = null;
  this.freeRows = null;
  this.freeMins = null;
  this.queenRows = null;
}
Queens.prototype = Object.create(benchmark.Benchmark.prototype);

Queens.prototype.benchmark = function () {
  var result = true;
  for (var i = 0; i < 10; i++) {
    result = result && this.queens();
  }
  return result;
};

Queens.prototype.verifyResult = function (result) {
  return result;
};

Queens.prototype.queens = function () {
  this.freeRows  = new Array( 8).fill(true);
  this.freeMaxs  = new Array(16).fill(true);
  this.freeMins  = new Array(16).fill(true);
  this.queenRows = new Array( 8).fill(-1);

  return this.placeQueen(0);
};

Queens.prototype.placeQueen = function (c) {
  for (var r = 0; r < 8; r++) {
    if (this.getRowColumn(r, c)) {
      this.queenRows[r] = c;
      this.setRowColumn(r, c, false);

      if (c == 7) {
        return true;
      }

      if (this.placeQueen(c + 1)) {
        return true;
      }
      this.setRowColumn(r, c, true);
    }
  }
  return false;
};

Queens.prototype.getRowColumn = function (r, c) {
  return this.freeRows[r] && this.freeMaxs[c + r] && this.freeMins[c - r + 7];
};

Queens.prototype.setRowColumn = function (r, c, v) {
  this.freeRows[r        ] = v;
  this.freeMaxs[c + r    ] = v;
  this.freeMins[c - r + 7] = v;
};

exports.newInstance = function () {
  return new Queens();
};
