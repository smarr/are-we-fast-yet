'use strict';

function Benchmark() {
  var that = this;

  this.innerBenchmarkLoop = function (innerIterations) {
    for (var i = 0; i < innerIterations; i++) {
      if (!that.verifyResult(that.benchmark())) {
        return false;
      }
    }
    return true;
  };

  this.benchmark = function () {
    throw "subclass responsibility"
  };

  this.verifyResult = function () {
    throw "subclass responsibility"
  };
}

exports.Benchmark = Benchmark;
