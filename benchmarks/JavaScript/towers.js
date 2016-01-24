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
  if (top != null  &&  disk.size >= top.size) {
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
