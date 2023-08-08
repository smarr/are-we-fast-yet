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

#include "List.h"
#include "Object.h"

// NOTE:
// with refcount: 77us + no leaks
// without refcount: 68us + some memory leaks
//    in tail() some Element are disposed without delete
// compare with 90us in generated C99 with Boehm GC

class Element : public Object {
    int val;
    Ref<Element> next;

public:
    Element(int v):next(0) {
        val = v;
    }
    ~Element()
    {
        setNext(0);
    }

    int length() {
        if (next == 0) {
            return 1;
        } else {
            return 1 + next->length();
        }
    }

    int getVal() { return val; }
    void setVal(int v) { val = v; }
    Element* getNext() { return next; }
    void setNext(Element* e)
    {
        next = e;
    }
};

static Ref<Element> makeList(int length) {
    if (length == 0) {
        return 0;
    } else {
        Ref<Element> e = new Element(length);
        e->setNext(makeList(length - 1));
        return e;
    }
}

static bool isShorterThan(Element* x, Element* y) {
    Element* xTail = x;
    Element* yTail = y;

    while (yTail != 0) {
        if (xTail == 0) {
            return true;
        }
        xTail = xTail->getNext();
        yTail = yTail->getNext();
    }
    return false;
}

static Ref<Element> tail(Element* x, Element* y, Element* z) {
    if (isShorterThan(y, x)) {
        return tail(tail(x->getNext(), y, z),
                    tail(y->getNext(), z, x),
                    tail(z->getNext(), x, y));
    } else {
        return z;
    }
}

int List::benchmark()
{
    Ref<Element> result = tail(makeList(15), makeList(10), makeList(6));
    const int res = result->length();
    return res;
}
