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

function TowersDisk(size) {
  this.size = size;
  this.next = null;
}

function Towers() {
  benchmark.Benchmark.call(this);
  this.piles = null;
  this.movesDone = 0;
}
Towers.prototype = Object.create(benchmark.Benchmark.prototype);

Towers.prototype.benchmark = function () {
  this.piles = new Array(3);
  this.buildTowerAt(0, 13);
  this.movesDone = 0;
  this.moveDisks(13, 0, 1);
  return this.movesDone;
};

Towers.prototype.verifyResult = function (result) {
  return 8191 === result;
};

Towers.prototype.pushDisk = function (disk, pile) {
  var top = this.piles[pile];
  if (top && disk.size >= top.size) {
    throw "Cannot put a big disk on a smaller one";
  }

  disk.next = top;
  this.piles[pile] = disk;
};

Towers.prototype.popDiskFrom = function (pile) {
  var top = this.piles[pile];
  if (top === null) {
    throw "Attempting to remove a disk from an empty pile";
  }

  this.piles[pile] = top.next;
  top.next = null;
  return top;
};

Towers.prototype.moveTopDisk = function (fromPile, toPile) {
  this.pushDisk(this.popDiskFrom(fromPile), toPile);
  this.movesDone += 1;
};

Towers.prototype.buildTowerAt = function (pile, disks) {
  for (var i = disks; i >= 0; i--) {
    this.pushDisk(new TowersDisk(i), pile);
  }
};

Towers.prototype.moveDisks = function (disks, fromPile, toPile) {
  if (disks == 1) {
    this.moveTopDisk(fromPile, toPile);
  } else {
    var otherPile = (3 - fromPile) - toPile;
    this.moveDisks(disks - 1, fromPile, otherPile);
    this.moveTopDisk(fromPile, toPile);
    this.moveDisks(disks - 1, otherPile, toPile);
  }
};

exports.newInstance = function () {
  return new Towers();
};
