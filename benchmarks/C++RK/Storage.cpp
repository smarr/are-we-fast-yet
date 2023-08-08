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

#include "Storage.h"
#include "som/Random.h"

#ifdef _USE_STD_VECTOR_

int Storage::benchmark()
{
    som::Random::reset();
    count = 0;
    Tree t;
    buildTreeDepth(7, &t);
    return count;
}

void Storage::buildTreeDepth(int depth, Tree* out)
{
    count++;
    if (depth == 1) {
        const int len = som::Random::next() % 10 + 1;
        out->subs.resize(len);
        for( int i = 0; i < len; i++ )
            out->subs[i] = new Tree();
    } else {
        const int len = 4;
        out->subs.resize(len);
        for( int i = 0; i < len; i++ )
        {
            out->subs[i] = new Tree();
            buildTreeDepth(depth - 1, out->subs[i]);
        }
    }
}

#else

int Storage::benchmark()
{
    som::Random::reset();
    count = 0;
    Tree* t = buildTreeDepth(7);
    delete[] t;
    return count;
}

Storage::Tree *Storage::buildTreeDepth(int depth)
{
    count++;
    if (depth == 1) {
        const int len = som::Random::next() % 10 + 1;
        return new Tree[len];
    } else {
        const int len = 4;
        Tree* arr = new Tree[len];
        for( int i = 0; i < len; i++ )
            arr[i].sub = buildTreeDepth(depth - 1);
        return arr;
    }
}

#endif
