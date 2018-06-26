// Copyright (c) 2001-2015 see AUTHORS file
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

class newPermute -> Benchmark {
  inherit harness.newBenchmark

  var count: Number := 0.asInteger
  var v: List := Done

  method benchmark -> Number {
    count := 0.asInteger
    v := platform.kernel.Array.new(6.asInteger)withAll(0.asInteger)
    permute(6.asInteger)
    count
  }

  method verifyResult(result: Number) -> Boolean {
    8660 == result
  }

  method permute(n: Number) -> Done {
    count := count + 1.asInteger
    (n != 0).ifTrue {
      permute (n - 1.asInteger)
      n.downTo(1.asInteger) do { i: Number ->
        swap (n) with (i)
        permute (n - 1.asInteger)
        swap (n) with (i)
      }
    }
  }

  method swap (i: Number) with (j: Number) -> Done {
    var tmp: Number := v.at(i)
    v. at (i) put (v.at(j))
    v. at (j) put (tmp)
  }
}

method newInstance -> Benchmark { newPermute }
