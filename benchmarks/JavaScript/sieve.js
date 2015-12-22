'use strict';

var benchmark = require('./benchmark.js');

function Sieve() {
  benchmark.Benchmark.call(this);
}
Sieve.prototype = Object.create(benchmark.Benchmark.prototype);

function sieve(flags, size) {
  var primeCount = 0;
  flags.fill(true);

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
  var flags = new Array(5000).fill(false);
  return sieve(flags, 5000);
};

Sieve.prototype.verifyResult = function (result) {
  return 669 == result;
};

exports.newInstance = function () {
  return new Sieve();
};
