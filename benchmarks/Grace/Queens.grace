// Copyright (c) 2001-2018 see AUTHORS file
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

class newQueens -> Benchmark {
  inherit harness.newBenchmark

  var freeMaxs: List := Done
  var freeRows: List := Done
  var freeMins: List := Done
  var queenRows: List := Done

  method benchmark -> Boolean {
    var result: Boolean := true

    1.asInteger.to(10.asInteger) do { j: Number ->
      result := result.and(queens)
    }
    result
  }

  method verifyResult(result: Boolean) -> Boolean {
    return result
  }

  method queens -> Boolean {
    freeRows  := platform.kernel.Array.new( 8.asInteger)withAll(true)
    freeMaxs  := platform.kernel.Array.new(16.asInteger)withAll(true)
    freeMins  := platform.kernel.Array.new(16.asInteger)withAll(true)
    queenRows := platform.kernel.Array.new( 8.asInteger)withAll(-1)
    placeQueen(1.asInteger)
  }

  method placeQueen (c: Number) -> Boolean {
    1.asInteger.to(8.asInteger) do { r: Number ->
      row (r) column (c) .ifTrue {
        queenRows.at (r) put (c)
        row (r) column (c) put (false)

        (c == 8).ifTrue { return true }
        placeQueen(c + 1.asInteger).ifTrue { return true }
        row (r) column (c) put (true)
      }
    }

    false
  }

  method row (r: Number) column (c: Number) -> Boolean {
    freeRows.at(r) && freeMaxs.at(c + r) && freeMins.at(c - r + 8.asInteger)
  }

  method row (r: Number) column (c: Number) put (v: Boolean) -> Done {
    freeRows.at( r                   ) put (v)
    freeMaxs.at( c + r               ) put (v)
    freeMins.at( c - r + 8.asInteger ) put (v)
    Done
  }
}

method newInstance -> Benchmark { newQueens }
