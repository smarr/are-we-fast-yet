// Copyright (c) 2001-2018 see AUTHORS file
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
// 
// Mmm... Hanoi...
//
//
// Adapted for Grace by Richard Roberts
//   2018, June
//

class Disk(size) {
    var next
    method setNext(item) { next := item }
    method getSize { size }
}

class Towers {

  var piles := platform.kernel.Array.new(4.asInteger)
  var movesDone := 0.asInteger

  method pushDisk (disk) onPile (pile) {
    var top := piles.at(pile)
    
    (!top.isNil).ifTrue {
      (disk.getSize >= top.getSize).ifTrue {
        error("Cannot put a bigger disk on a smaller one")
      }
    }

    disk.setNext(top)
    piles.at(pile)put(disk)
  }

  method popDiskFrom(pile) {
    var top := piles.at(pile)
    (top.isNil).ifTrue {
      error("Attempting to remove a disk from an empty pile")
    }

    piles.at(pile)put(top.next)
    top.setNext(Done)
    top
  }

  method moveTopDiskFrom (fromPile) to (toPile) {
    var disk := popDiskFrom (fromPile)
    pushDisk (disk) onPile (toPile)
    movesDone := movesDone + 1.asInteger
  }

  method buildTowerAt(pile) disks(disks) {
    disks.downTo(1.asInteger) do { size ->
      var disk := Disk(size)
      pushDisk (disk) onPile (pile)
    }
  }

  method move (disks) disksFrom (fromPile) to (toPile) {
    (disks == 1.asInteger).ifTrue {
      moveTopDiskFrom (fromPile) to (toPile)
    } ifFalse {
      var otherPile := 6.asInteger - fromPile - toPile
      move (disks - 1.asInteger) disksFrom (fromPile) to (otherPile)
      moveTopDiskFrom (fromPile) to (toPile)
      move (disks - 1.asInteger) disksFrom (otherPile) to (toPile)
    }
  }
}

method asString {
  "Towers.grace"
}

method benchmark(innerIterations) -> Done {
  1.asInteger.to(innerIterations) do { i ->
    var towers := Towers
    towers.buildTowerAt(1.asInteger) disks(13.asInteger)
    towers.move(13.asInteger) disksFrom(1.asInteger) to(2.asInteger)
    (towers.movesDone != 8191.asInteger).ifTrue {
      error("{self} failed, {towers.movesDone} != 8191")
    }
  }
}
