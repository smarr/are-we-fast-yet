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

class newSieve -> Benchmark {
  inherit harness.newBenchmark

  method benchmark -> Number {
    var flags: List := platform.kernel.Array.new(5000.asInteger)withAll(true)
    return sieve(flags)size(5000.asInteger)
  }

  method verifyResult(result: Number) -> Boolean {
    669.asInteger == result
  }

  method sieve(flags: List) size(size: Number) -> Number {
    var primeCount: Number := 0.asInteger

    2.asInteger.to(size) do { i: Number ->
      flags.at(i - 1.asInteger).ifTrue {
        primeCount := primeCount + 1.asInteger

        var k: Number := i + i
        { k <= size }.whileTrue {
          flags.at (k - 1.asInteger) put (false)
          k := k + i
        }
      }
    }

    primeCount
  }
}

method newInstance -> Benchmark { newSieve }
