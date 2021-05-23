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


class Queens(Benchmark):
    def __init__(self):
        self._free_maxs = None
        self._free_rows = None
        self._free_mins = None
        self._queen_rows = None

    def benchmark(self):
        result = True
        for _ in range(10):
            result = result and self.queens()
        return result

    def verify_result(self, result):
        return result

    def queens(self):
        self._free_rows = [True] * 8
        self._free_maxs = [True] * 16
        self._free_mins = [True] * 16
        self._queen_rows = [-1] * 8

        return self.place_queen(0)

    def place_queen(self, c):
        for r in range(8):
            if self.get_row_column(r, c):
                self._queen_rows[r] = c
                self.set_row_column(r, c, False)

                if c == 7:
                    return True
                if self.place_queen(c + 1):
                    return True

                self.set_row_column(r, c, True)
        return False

    def get_row_column(self, r, c):
        return (
            self._free_rows[r] and self._free_maxs[c + r] and self._free_mins[c - r + 7]
        )

    def set_row_column(self, r, c, v):
        self._free_rows[r] = v
        self._free_maxs[c + r] = v
        self._free_mins[c - r + 7] = v
