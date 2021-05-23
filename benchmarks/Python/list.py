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


class Element:
    def __init__(self, v):
        self._val = v
        self.next = None

    def length(self):
        if self.next is None:
            return 1
        return 1 + self.next.length()


class List(Benchmark):
    def benchmark(self):
        result = self.tail(self.make_list(15), self.make_list(10), self.make_list(6))
        return result.length()

    def make_list(self, length):
        if length == 0:
            return None

        e = Element(length)
        e.next = self.make_list(length - 1)
        return e

    @staticmethod
    def is_shorter_than(x, y):
        x_tail = x
        y_tail = y

        while y_tail is not None:
            if x_tail is None:
                return True

            x_tail = x_tail.next
            y_tail = y_tail.next

        return False

    def tail(self, x, y, z):
        if self.is_shorter_than(y, x):  # pylint: disable=arguments-out-of-order
            return self.tail(
                self.tail(x.next, y, z),
                self.tail(y.next, z, x),
                self.tail(z.next, x, y),
            )
        return z

    def verify_result(self, result):
        return result == 10
