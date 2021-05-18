# This code is based on the SOM class library.
#
# Copyright (c) 2001-2016 see AUTHORS.md file
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
from som.vector import Vector

_INITIAL_CAPACITY = 16


class Entry:
    def __init__(self, hash_, key, value, next_):
        self.hash = hash_
        self.key = key
        self.value = value
        self.next = next_

    def match(self, hash_, key):
        return self.hash == hash_ and key == self.key


def _hash(key):
    if key is None:
        return 0

    hash_ = key.custom_hash()
    return hash_ ^ hash_ >> 16


class Dictionary:
    def __init__(self, size=_INITIAL_CAPACITY):
        self._buckets = [None] * size
        self._size = 0

    def size(self):
        return self._size

    def is_empty(self):
        return self._size == 0

    def _get_bucket_idx(self, hash_):
        return (len(self._buckets) - 1) & hash_

    def _get_bucket(self, hash_):
        return self._buckets[self._get_bucket_idx(hash_)]

    def at(self, key):
        hash_ = _hash(key)
        e = self._get_bucket(hash_)

        while e is not None:
            if e.match(hash_, key):
                return e.value
            e = e.next
        return None

    def contains_key(self, key):
        hash_ = _hash(key)
        e = self._get_bucket(hash_)

        while e is not None:
            if e.match(hash_, key):
                return True
            e = e.next
        return False

    def at_put(self, key, value):
        hash_ = _hash(key)
        i = self._get_bucket_idx(hash_)

        current = self._buckets[i]

        if current is None:
            self._buckets[i] = self._new_entry(key, value, hash_)
            self._size += 1
        else:
            self._insert_bucket_entry(key, value, hash_, current)

        if self._size > len(self._buckets):
            self._resize()

    def _new_entry(self, key, value, hash_):
        return Entry(hash_, key, value, None)

    def _insert_bucket_entry(self, key, value, hash_, head):
        current = head

        while True:
            if current.match(hash_, key):
                current.value = value
                return

            if current.next is None:
                self._size += 1
                current.next = self._new_entry(key, value, hash_)
                return

            current = current.next

    def _resize(self):
        old_storage = self._buckets

        new_storage = [None] * len(old_storage) * 2
        self._buckets = new_storage
        self._transfer_entries(old_storage)

    def _transfer_entries(self, old_storage):
        for i in range(len(old_storage)):  # pylint: disable=consider-using-enumerate
            current = old_storage[i]
            if current is not None:
                old_storage[i] = None

                if current.next is None:
                    self._buckets[current.hash & (len(self._buckets) - 1)] = current
                else:
                    self._split_bucket(old_storage, i, current)

    def _split_bucket(self, old_storage, i, head):
        lo_head = None
        lo_tail = None
        hi_head = None
        hi_tail = None
        current = head

        while current is not None:
            if (current.hash & len(old_storage)) == 0:
                if lo_tail is None:
                    lo_head = current
                else:
                    lo_tail.next = current
                lo_tail = current
            else:
                if hi_tail is None:
                    hi_head = current
                else:
                    hi_tail.next = current
                hi_tail = current
            current = current.next

        if lo_tail is not None:
            lo_tail.next = None
            self._buckets[i] = lo_head
        if hi_tail is not None:
            hi_tail.next = None
            self._buckets[i + len(old_storage)] = hi_head

    def remove_all(self):
        self._buckets = [None] * len(self._buckets)
        self._size = 0

    def get_keys(self):
        keys = Vector(self._size)
        for i in range(len(self._buckets)):
            current = self._buckets[i]
            while current is not None:
                keys.append(current.key)
                current = current.next
        return keys

    def get_values(self):
        values = Vector(self._size)
        for i in range(len(self._buckets)):
            current = self._buckets[i]
            while current is not None:
                values.append(current.value)
                current = current.next
        return values
