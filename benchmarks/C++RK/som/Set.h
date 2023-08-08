#ifndef SOM_SET_H
#define SOM_SET_H

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

#include <som/Vector.h>

namespace som {

template<class E>
class Set {
    Vector<E> items;

    enum { INITIAL_SIZE = 10 };

public:
    Set()
    {
        items.expand(INITIAL_SIZE);
    }

    Set(int size) {
        items.expand(size);
    }

    int size() {
        return items.size();
    }

    void forEach(ForEachInterface<E>& fn) {
        items.forEach(fn);
    }

    bool hasSome(TestInterface<E>& fn) {
        return items.hasSome(fn);
    }

    void add(const E& obj) {
        if (!contains(obj)) {
            items.append(obj);
        }
    }

    void collect(CollectInterface<E, E>& fn, Vector<E>& coll) {
        coll.removeAll();

        struct Iterator : public ForEachInterface<E>
        {
            Iterator(CollectInterface<E, E>& f, Vector<E>& c):coll(c),fn(f){}
            Vector<E>& coll;
            CollectInterface<E, E>& fn;
            void apply(const E& e)
            {
                coll.append(fn.collect(e));
            }
        } it(coll);
        forEach(it);
    }

    bool contains(const E& obj) {
        struct Iterator : public TestInterface<E>
        {
            Iterator(const E& o):obj(o){}
            const E& obj;
            bool test(const E& elem) const { return elem == obj; }
        } it(obj);
        return hasSome(it);
    }

    void removeAll() {
        items.removeAll();
    }
};

template<class E>
class IdentitySet : public Set<E> {
public:
  IdentitySet() {}
  IdentitySet(int size):Set<E>(size) {}

};
}

#endif // SOM_SET_H
