// @ts-check
// This code is derived from the SOM benchmarks, see AUTHORS.md file.
//
// Copyright (c) 2015-2016 Stefan Marr <git@stefan-marr.de>
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

const { Benchmark } = require('./benchmark');
const { Random } = require('./som');

class Storage extends Benchmark {
  constructor() {
    super();
    this.count = 0;
  }

  benchmark() {
    const random = new Random();
    this.count = 0;
    this.buildTreeDepth(7, random);
    return this.count;
  }

  verifyResult(result) {
    return 5461 === result;
  }

  buildTreeDepth(depth, random) {
    this.count += 1;
    if (depth === 1) {
      return new Array((random.next() % 10) + 1);
    }
    const arr = new Array(4);
    for (let i = 0; i < 4; i += 1) {
      arr[i] = this.buildTreeDepth(depth - 1, random);
    }
    return arr;
  }
}

exports.newInstance = () => new Storage();
