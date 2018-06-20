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


method run {

  var flags: List := platform.kernel.Array.new(5000.asInteger)
  var size: Number := 5000
  var primeCount: Number := 0

  flags.putAll(true)

  2.asInteger.to(size.asInteger) do { i ->

    if (flags.at(i - 1.asInteger)) then {
      primeCount := primeCount + 1.asInteger

      var k: Number := i + i
      { k <= size.asInteger }.whileTrue {
        flags.at (k - 1.asInteger) put (false)
        k := k + i
      }

    }
  }
  
  primeCount
}

method asString -> String {
  "Sieve.grace"
}

method benchmark(innerIterations: Number) {
  1.asInteger.to(innerIterations) do { i ->
    var result: Number := run
    if (result != 669) then {
      error("{self} failed, {result} != 669")
    }
  }
}
