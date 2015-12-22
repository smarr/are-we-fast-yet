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
