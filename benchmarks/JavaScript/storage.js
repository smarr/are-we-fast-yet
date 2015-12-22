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
