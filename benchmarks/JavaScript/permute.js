'use strict';

var benchmark = require('./benchmark.js');

function Permute() {
  benchmark.Benchmark.call(this);
  this.count = 0;
  this.v     = null;
}
Permute.prototype = Object.create(benchmark.Benchmark.prototype);

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

exports.newInstance = function () {
  return new Permute();
};
