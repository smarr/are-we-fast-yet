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
from som.constants import INITIAL_SIZE
from som.vector import Vector


class Set:
    def __init__(self, size=INITIAL_SIZE):
        self._items = Vector(size)

    def size(self):
        return self._items.size()

    def for_each(self, block):
        self._items.for_each(block)

    def has_some(self, block):
        self._items.has_some(block)

    def get_one(self, block):
        self._items.get_one(block)

    def add(self, obj):
        if not self.contains(obj):
            self._items.append(obj)

    def collect(self, block):
        coll = Vector()
        self.for_each(lambda e: coll.append(block(e)))
        return coll

    def contains(self, obj):
        return self.has_some(lambda it: it == obj)
