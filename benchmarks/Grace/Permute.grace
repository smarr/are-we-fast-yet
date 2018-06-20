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

var count
var v

method permute (n) {
  count := count + 1.asInteger
  (n != 0). ifTrue {
    permute (n - 1.asInteger)
    n.downTo(1.asInteger) do { i -> 
      swap (n) with (i)
      permute (n - 1.asInteger)
      swap (n) with (i)
    }
  }
}

method swap (i) with (j) {
  var tmp := v.at(i)
  v. at (i) put (v.at(j))
  v. at (j) put (tmp)
}

method asString { "Permute.grace" }

method benchmark (innerIterations) {
  1.asInteger.to(innerIterations) do { i ->
    count := 0.asInteger
    v := platform.kernel.Array.new(7.asInteger)
    permute(6.asInteger)
    (count == 8660). ifFalse {
      error("{self} failed, {count} != 8660")
    }
  }
}