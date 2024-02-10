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


def vector_with(elem):
    v = Vector(1)
    v.append(elem)
    return v


# Porting notes:
#  - does not use an explicit array bounds check, because Java already does
#    that. Don't see a point in doing it twice.
class Vector:
    def __init__(self, size=0):
        self._storage = None if size == 0 else [None] * size
        self._first_idx = 0
        self._last_idx = 0

    def at(self, idx):
        if self._storage is None or idx >= len(self._storage):
            return None
        return self._storage[idx]

    def at_put(self, idx, val):
        if self._storage is None:
            self._storage = [None] * max(idx + 1, INITIAL_SIZE)
        elif idx >= len(self._storage):
            new_length = len(self._storage)
            while new_length <= idx:
                new_length *= 2

            new_storage = [None] * new_length
            for i in range(len(self._storage)):
                new_storage[i] = self._storage[i]
            self._storage = new_storage

        self._storage[idx] = val
        if self._last_idx < idx + 1:
            self._last_idx = idx + 1

    def append(self, elem):
        if self._storage is None:
            self._storage = [None] * INITIAL_SIZE
        elif self._last_idx >= len(self._storage):
            # Need to expand capacity first
            new_storage = [None] * (2 * len(self._storage))
            for i in range(len(self._storage)):
                new_storage[i] = self._storage[i]
            self._storage = new_storage

        self._storage[self._last_idx] = elem
        self._last_idx += 1

    def is_empty(self):
        return self._last_idx == self._first_idx

    def for_each(self, fn):
        for i in range(self._first_idx, self._last_idx):
            fn(self._storage[i])

    def has_some(self, fn):
        for i in range(self._first_idx, self._last_idx):
            if fn(self._storage[i]):
                return True
        return False

    def get_one(self, fn):
        for i in range(self._first_idx, self._last_idx):
            e = self._storage[i]
            if fn(e):
                return e
        return None

    def first(self):
        if self.is_empty():
            return None
        return self._storage[self._first_idx]

    def remove_first(self):
        if self.is_empty():
            return None
        self._first_idx += 1
        return self._storage[self._first_idx - 1]

    def remove(self, obj):
        if self._storage is None or self.is_empty():
            return False

        new_array = [None] * self.capacity()
        new_last = 0
        found = False

        def each(it):
            nonlocal new_last
            nonlocal found
            if it is obj:
                found = True
            else:
                new_array[new_last] = it
                new_last += 1

        self.for_each(each)

        self._storage = new_array
        self._last_idx = new_last
        self._first_idx = 0
        return found

    def remove_all(self):
        self._first_idx = 0
        self._last_idx = 0

        if self._storage is not None:
            self._storage = [None] * len(self._storage)

    def size(self):
        return self._last_idx - self._first_idx

    def capacity(self):
        return 0 if self._storage is None else len(self._storage)

    def sort(self, comparator):
        if self.size() > 0:
            self._sort(self._first_idx, self._last_idx - 1, comparator)

    def _sort(self, i, j, c):
        if c is None:
            self._default_sort(i, j)

        n = j + 1 - i
        if n <= 1:
            return

        di = self._storage[i]
        dj = self._storage[j]

        if c.compare(di, dj) > 0:
            self._swap(self._storage, i, j)
            di, dj = dj, di

        if n > 2:
            ij = (i + j) // 2
            dij = self._storage[ij]

        if c.compare(di, dij) <= 0:
            if c.compare(dij, dj) > 0:
                self._swap(self._storage, j, ij)
                dij = dj
        else:
            self._swap(self._storage, i, ij)
            dij = di

        if n > 3:
            k = i
            l = j - 1

            while True:
                while k <= l and c.compare(dij, self._storage[l]) <= 0:
                    l -= 1

                k += 1
                while k <= l and c.compare(self._storage[k], dij) <= 0:
                    k += 1

                if k > l:
                    break

                self._swap(self._storage, k, l)

            self._sort(i, l, c)
            self._sort(k, j, c)

    def _swap(self, storage, i, j):
        raise NotImplementedError()

    def _default_sort(self, i, j):
        raise NotImplementedError()
