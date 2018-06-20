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

type Disk = interface {
  next
  setNext(item)
  getSize
}

class Disk(size: Number) {
    var next: Disk
    method setNext(item: Disk) -> Done { next := item }
    method getSize -> Number { size }
}

type Towers = {
  piles
  movesDone

  pushDisk(disk)onPile(pile)
  popDiskFrom(pile)
  moveTopDiskFrom(fromPile)to(toPile)
  buildTowerAt(pile)disks(disks)
  move(disks)disksFrom(fromPile)to(toPile)
}

class Towers -> Towers {

  var piles: List := platform.kernel.Array.new(4.asInteger)
  var movesDone: Number := 0.asInteger

  method pushDisk (disk: Disk) onPile (pile: Number) -> Done {
    var top: Disk := piles.at(pile)
    
    (!top.isNil).ifTrue {
      (disk.getSize >= top.getSize).ifTrue {
        error("Cannot put a bigger disk on a smaller one")
      }
    }

    disk.setNext(top)
    piles.at(pile)put(disk)
  }

  method popDiskFrom(pile: Number) -> Disk {
    var top: Disk := piles.at(pile)
    (top.isNil).ifTrue {
      error("Attempting to remove a disk from an empty pile")
    }

    piles.at(pile)put(top.next)
    top.setNext(Done)
    top
  }

  method moveTopDiskFrom (fromPile: Number) to (toPile: Number) -> Done {
    var disk: Disk := popDiskFrom (fromPile)
    pushDisk (disk) onPile (toPile)
    movesDone := movesDone + 1.asInteger
  }

  method buildTowerAt(pile: Number) disks(disks: Number) -> Done {
    disks.downTo(1.asInteger) do { size: Number ->
      var disk: Disk := Disk(size)
      pushDisk (disk) onPile (pile)
    }
  }

  method move (disks: Number) disksFrom (fromPile: Number) to (toPile: Number) -> Done {
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

method asString -> String {
  "Towers.grace"
}

method benchmark(innerIterations: Number) -> Done {
  1.asInteger.to(innerIterations) do { i ->
    var towers: Towers := Towers
    towers.buildTowerAt(1.asInteger) disks(13.asInteger)
    towers.move(13.asInteger) disksFrom(1.asInteger) to(2.asInteger)
    (towers.movesDone != 8191.asInteger).ifTrue {
      error("{self} failed, {towers.movesDone} != 8191")
    }
  }
}
