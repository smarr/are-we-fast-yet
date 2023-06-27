#ifndef SOM_VECTOR_H
#define SOM_VECTOR_H

/* This code is derived from the SOM benchmarks, see AUTHORS.md file.
 *
 * Copyright (c) 2023 Rochus Keller <me@rochus-keller.ch> (for C++ migration)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the 'Software'), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

#include <som/Interfaces.h>

namespace som {

template<class E>
class Vector {
private:
    E* storage;
    int firstIdx;
    int lastIdx;
    int length;

    void enlarge(int idx)
    {
        int newLength = length;
        while (newLength <= idx) {
            newLength *= 2;
            newLength += 50;
        }
        expand(newLength);
    }

public:
    Vector(int size = 0):firstIdx(0),lastIdx(0),length(size),storage(0) {
        if( size )
            storage = new E[size];
    }

    ~Vector() {
        if( storage )
            delete[] storage;
    }

    Vector( const Vector<E>& rhs):
        firstIdx(0),lastIdx(0),length(0),storage(0)
    {
        *this = rhs;
    }

    Vector<E>& operator=( const Vector<E>& rhs)
    {
        if( length < rhs.lastIdx )
        {
            // not enough space to accommodate rhs
            if( storage )
                delete[] storage;
            if( rhs.length )
                storage = new E[rhs.length];
            else
                storage = 0;
            length = rhs.length;
        }
        firstIdx = rhs.firstIdx;
        lastIdx = rhs.lastIdx;
        if( rhs.length )
        {
            for( int i = firstIdx; i < lastIdx; i++ )
                storage[i] = rhs.storage[i];
        }
        return *this;
    }

    void expand(int newLength)
    {
        if( newLength <= length )
            return;
        E* newStorage = new E[newLength];
        for( int i = firstIdx; i < lastIdx; i++ )
            newStorage[i] = storage[i];
        if( storage )
            delete[] storage;
        storage = newStorage;
        length = newLength;
    }

    const E& at(int idx) const {
        if (idx < 0 || idx >= length) {
            throw "out of bounds";
        }
        return storage[idx];
    }

    E& at(int idx) {
        if (idx < 0 || idx >= length) {
            throw "out of bounds";
        }
        return storage[idx];
    }

    void atPut(int idx, const E& val) {
        if (idx >= length)
            enlarge(idx);
        storage[idx] = val;
        if (lastIdx < idx + 1) {
            lastIdx = idx + 1;
        }
    }

    void append(const E& elem) {
        if (lastIdx >= length)
            enlarge(lastIdx);

        storage[lastIdx] = elem;
        lastIdx++;
    }

    bool isEmpty() const {
        return lastIdx == firstIdx;
    }

    void forEach(ForEachInterface<E>& fn) {
        for (int i = firstIdx; i < lastIdx; i++) {
            fn.apply(storage[i]);
        }
    }

    bool hasSome(TestInterface<E>& fn) const {
        for (int i = firstIdx; i < lastIdx; i++) {
            if (fn.test(storage[i])) {
                return true;
            }
        }
        return false;
    }

    E& removeFirst() {
        if (isEmpty()) {
            throw "empty";
        }
        firstIdx++;
        return storage[firstIdx - 1];
    }

    bool remove(const E& obj) {
        if( length == 0 )
            return false;

        E* newArray = new E[length];

        struct Iterator : public ForEachInterface<E>
        {
            Iterator(E* a, const E* o):newArray(a),newLast(0),found(false),obj(o){}
            E* newArray;
            int newLast;
            const E* obj;
            bool found;
            void apply(const E& it)
            {
                if (it == *obj) {
                    found = true;
                } else {
                    newArray[newLast] = it;
                    newLast++;
                }
            }
        } it(newArray, &obj);
        forEach(it);

        delete[] storage;
        storage  = newArray;
        lastIdx  = it.newLast;
        firstIdx = 0;
        return it.found;
    }

    void removeAll() {
        firstIdx = 0;
        lastIdx = 0;
        if( length )
        {
            delete[] storage;
            storage = new E[length];
        }
    }

    int size() const {
        return lastIdx - firstIdx;
    }

    int capacity() const {
        return length;
    }

    void sort( const Comparator<E>& c) {
        if (size() > 0) {
            sort(firstIdx, lastIdx - 1, c);
        }
    }

    void sort(int i, int j, const Comparator<E>& c) {
#if 0
        if (c == 0) {
            defaultSort(i, j);
        }
#endif

        int n = j + 1 - i;
        if (n <= 1) {
            return;
        }

        E di = storage[i];
        E dj = storage[j];

        if (c.compare(di, dj) > 0) {
            swap(storage, i, j);
            E tt = di;
            di = dj;
            dj = tt;
        }

        if (n > 2) {
            int ij = (i + j) / 2;
            E dij = storage[ij];

            if (c.compare(di, dij) <= 0) {
                if (c.compare(dij, dj) > 0) {
                    swap(storage, j, ij);
                    dij = dj;
                }
            } else {
                swap(storage, i, ij);
                dij = di;
            }

            if (n > 3) {
                int k = i;
                int l = j - 1;

                while (true) {
                    while (k <= l && c.compare(dij, storage[l]) <= 0) {
                        l -= 1;
                    }

                    k += 1;
                    while (k <= l && c.compare( storage[k], dij) <= 0) {
                        k += 1;
                    }

                    if (k > l) {
                        break;
                    }
                    swap(storage, k, l);
                }

                sort(i, l, c);
                sort(k, j, c);
            }
        }
    }

    static void swap(E* storage2, int i, int j) {
        throw "NotImplemented";
    }

    static void defaultSort(int i, int j) {
        throw "NotImplemented";
    }
};

}

#endif // SOM_VECTOR_H
