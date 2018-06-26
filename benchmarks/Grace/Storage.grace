// Copyright (c) 2018 see AUTHORS.md file
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

import "harness" as harness

class newStorage -> Benchmark {
  inherit harness.newBenchmark

  var count: Number := 0.asInteger

  method benchmark -> Number {
    def random: Random = harness.newRandom
    count := 0.asInteger
    buildTreeDepth(7.asInteger)with(random)
    count
  }

  method verifyResult(result: Number) -> Boolean {
    5461.asInteger == result
  }

  method buildTreeDepth(depth: Number)with(random: Random) -> List {
    count := count + 1.asInteger
    return (depth == 1.asInteger).ifTrue {
      platform.kernel.Array.new((random.next % 10.asInteger) + 1.asInteger)
    } ifFalse {
      platform.kernel.Array.new(4.asInteger)
                            withAll { buildTreeDepth(depth - 1.asInteger) with (random) }
    }
  }
}

method newInstance -> Benchmark { newStorage }
