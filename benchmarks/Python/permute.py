# This code is based on the SOM class library.
#
# Copyright (c) 2001-2021 see AUTHORS.md file
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the 'Software'), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
from benchmark import Benchmark


class Permute(Benchmark):
    def __init__(self):
        self._count = 0
        self._v = None

    def benchmark(self):
        self._count = 0
        self._v = [0] * 6
        self._permute(6)

        return self._count

    def _permute(self, n):
        self._count += 1
        if n != 0:
            n1 = n - 1
            self._permute(n1)
            for i in range(n1, -1, -1):
                self._swap(n1, i)
                self._permute(n1)
                self._swap(n1, i)

    def _swap(self, i, j):
        tmp = self._v[i]
        self._v[i] = self._v[j]
        self._v[j] = tmp

    def verify_result(self, result):
        return result == 8660
