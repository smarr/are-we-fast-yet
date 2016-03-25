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

var INITIAL_SIZE = 10;

function Pair(key, val) {
  this.key   = key;
  this.value = val;
}

function Vector(size) {
  this.storage  = new Array(size === undefined ? 50 : size);
  this.firstIdx = 0;
  this.lastIdx  = 0;
}

Vector.with = function (elem) {
  var v = new Vector(1);
  v.append(elem);
  return v;
};

Vector.prototype.at = function (idx) {
  return this.storage[idx];
};

Vector.prototype.append = function (elem) {
  if (this.lastIdx >= this.storage.length) {
    // Copy storage to comply with rules, but don't extend storage
    var newLength = this.storage.length * 2;
    this.storage = this.storage.slice();
    this.storage.length = newLength;
  }

  this.storage[this.lastIdx] = elem;
  this.lastIdx += 1;
};

Vector.prototype.isEmpty = function () {
  return this.lastIdx === this.firstIdx;
};

Vector.prototype.forEach = function (fn) {
  for (var i = this.firstIdx; i < this.lastIdx; i++) {
    fn(this.storage[i]);
  }
};

Vector.prototype.hasSome = function(fn) {
  for (var i = this.firstIdx; i < this.lastIdx; i++) {
    if (fn(this.storage[i])) {
      return true;
    }
  }
  return false;
};

Vector.prototype.getOne = function (fn) {
  for (var i = this.firstIdx; i < this.lastIdx; i++) {
    var e = this.storage[i];
    if (fn(e)) {
      return e;
    }
  }
  return null;
};

Vector.prototype.removeFirst = function () {
  if (this.isEmpty()) {
    return null;
  }
  this.firstIdx++;
  return this.storage[this.firstIdx - 1];
};

Vector.prototype.remove = function (obj) {
  var newArray = new Array(this.capacity());
  var newLast = 0;
  var found = false;

  this.forEach(function (it) {
    if (it == obj) {
      found = true;
    } else {
      newArray[newLast] = it;
      newLast++;
    }
  });

  this.storage  = newArray;
  this.lastIdx  = newLast;
  this.firstIdx = 0;
  return found;
};

Vector.prototype.size = function () {
  return this.lastIdx - this.firstIdx;
};

Vector.prototype.capacity = function () {
  return this.storage.length;
};

Vector.prototype.sortRange = function (i, j, compare) {
  if (compare === null) {
    this.defaultSort(i, j);
  }

  var n = j + 1 - i;
  if (n <= 1) {
    return;
  }

  var di = this.storage[i];
  var dj = this.storage[j];

  if (compare(di, dj)) {
    this.swap(this.storage, i, j);
    var tt = di;
    di = dj;
    dj = tt;
  }

  if (n > 2) {
    var ij = (i + j) / 2;
    var dij = this.storage[ij];

    if (compare(di, dij) <= 0) {
      if (!compare(dij, dj)) {
        this.swap(this.storage, j, ij);
        dij = dj;
      }
    } else {
      this.swap(this.storage, i, ij);
      dij = di;
    }

    if (n > 3) {
      var k = i;
      var l = j - 1;

      while (true) {
        while (k <= l && compare(dij, this.storage[l])) {
          l -= 1;
        }

        k += 1;
        while (k <= l && compare(this.storage[k], dij)) {
          k += 1;
        }

        if (k > l) {
          break;
        }
        this.swap(this.storage, k, l);
      }

      this.sort(i, l, c);
      this.sort(k, j, c);
    }
  }
};

Vector.prototype.sort = function(compare) {
  if (this.size() > 0) {
    this.sortRange(this.firstIdx, this.lastIdx - 1, compare);
  }
};

function Set(size) {
  this.items = new Vector(size === undefined ? INITIAL_SIZE : size);
}

Set.prototype.forEach = function (fn) {
  this.items.forEach(fn);
};

Set.prototype.hasSome = function (fn) {
  return this.items.hasSome(fn);
};

Set.prototype.getOne = function (fn) {
  return this.items.getOne(fn);
};

Set.prototype.add = function (obj) {
  if (!this.contains(obj)) {
    this.items.append(obj);
  }
};

Set.prototype.collect = function (fn) {
  var coll = new Vector();

  this.forEach(function (e) {
    coll.append(fn(e));
  });

  return coll;
};

function IdentitySet(size) {
  Set.call(this, size === undefined ? INITIAL_SIZE : size);
}
IdentitySet.prototype = Object.create(Set.prototype);

IdentitySet.prototype.contains = function (obj) {
  return this.hasSome(function (e) { return e == obj; })
};

function Dictionary (size) {
  this.pairs = new IdentitySet(size === undefined ? INITIAL_SIZE : size);
}

Dictionary.prototype.atPut = function (key, value) {
  var pair = this.pairAt(key);
  if (pair === null) {
    this.pairs.add(new Pair(key, value));
  } else {
    pair.value = value;
  }
};

Dictionary.prototype.at = function (key) {
  var pair = this.pairAt(key);
  if (pair === null) {
    return null;
  } else {
    return pair.value;
  }
};

Dictionary.prototype.pairAt = function (key) {
  return this.pairs.getOne(function (p) {
    return p.key === key
  })
};

Dictionary.prototype.getKeys = function () {
  return this.pairs.collect(function (p) { return p.key });
};

function Random() {
  this.seed = 74755;
}

Random.prototype.next = function () {
  this.seed = ((this.seed * 1309) + 13849) & 65535;
  return this.seed;
};

exports.IdentitySet = IdentitySet;
exports.Dictionary  = Dictionary;
exports.Vector      = Vector;
exports.Random      = Random;
