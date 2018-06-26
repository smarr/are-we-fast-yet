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
//
// Adapted for Grace by Richard Roberts
//   2018, June
//

import "harness" as harness

type ListElement = interface {
  val
  next
  length
}

class newListElement(n: Number) -> ListElement {
  var val: Number := n
  var next: ListElement := Done

  method length -> Number {
    next.isNil.ifTrue {
      return 1.asInteger
    } ifFalse {
      return 1.asInteger + next.length
    }
  }
}

class newList -> Benchmark {
  inherit harness.newBenchmark

  method benchmark -> Number {
    def result: ListElement = talkWithX (makeList(15))
                                  withY (makeList(10))
                                  withZ (makeList( 6))
    return result.length
  }

  method verifyResult(result: Number) -> Boolean {
    result == 10
  }

  method makeList(length: Number) -> ListElement {
    (length == 0).ifTrue {
      return Done
    } ifFalse {
      var e: ListElement := newListElement(length)
      e.next(makeList(length - 1.asInteger))
      return e
    }
  }

  method isShorter (x: ListElement) than (y: ListElement) -> Boolean {
    var xTail: ListElement := x
    var yTail: ListElement := y

    { yTail.isNil }.whileFalse {
        xTail.isNil.ifTrue {
          return true
        }
        xTail := xTail.next
        yTail := yTail.next
    }
    false
  }

  method talkWithX (x: ListElement) withY (y: ListElement) withZ (z: ListElement) -> ListElement {
    (isShorter (y) than (x)).ifTrue {
      return talkWithX (talkWithX (x.next) withY (y) withZ (z) )
                 withY (talkWithX (y.next) withY (z) withZ (x) )
                 withZ (talkWithX (z.next) withY (x) withZ (y) )
    } ifFalse {
      return z
    }
  }
}

method newInstance -> Benchmark { newList }
