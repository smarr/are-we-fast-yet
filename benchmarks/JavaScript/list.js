'use strict';

var benchmark = require('./benchmark.js');

function List() {
  benchmark.Benchmark.call(this);
}
List.prototype = Object.create(benchmark.Benchmark.prototype);

List.prototype.benchmark = function () {
  var result = this.tail(this.makeList(15),
                         this.makeList(10),
                         this.makeList(6));
  return result.length();
};

List.prototype.makeList = function (length) {
  if (length === 0) {
    return null;
  } else {
    var e = new Element(length);
    e.next = this.makeList(length - 1);
    return e;
  }
};

List.prototype.isShorterThan = function (x, y) {
  var xTail = x,
    yTail   = y;

  while (yTail !== null) {
    if (xTail === null) { return true; }
    xTail = xTail.next;
    yTail = yTail.next;
  }
  return false;
};

List.prototype.tail = function (x, y, z) {
  if (this.isShorterThan(y, x)) {
    return this.tail(this.tail(x.next, y, z),
      this.tail(y.next, z, x),
      this.tail(z.next, x, y));
  } else {
    return z;
  }
};

List.prototype.verifyResult = function (result) {
  return 10 === result;
};

function Element(v) {
  this.val  = v;
  this.next = null;
}

Element.prototype.length = function () {
  if (this.next === null) {
    return 1;
  } else {
    return 1 + this.next.length();
  }
};

exports.newInstance = function () {
  return new List();
};
