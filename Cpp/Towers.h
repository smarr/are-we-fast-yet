#ifndef _TOWERS_H
#define _TOWERS_H

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

#include "Benchmark.h"

class Towers : public Benchmark {

    class TowersDisk {
        int size;
        TowersDisk* next;

    public:
        TowersDisk(int size):next(0) {
            this->size = size;
        }
        ~TowersDisk()
        {
            if( next )
                delete next;
        }

        int  getSize() const { return size;  }

        TowersDisk* getNext() const { return next;  }
        void setNext(TowersDisk* value) { next = value; }
    };

    enum { PilesCount = 3 };
    TowersDisk* piles[PilesCount];
    int movesDone;

    void pushDisk( TowersDisk* disk, int pile);

    TowersDisk* popDiskFrom(int pile);

    void moveTopDisk(int fromPile, int toPile);

    void buildTowerAt(int pile, int disks);

    void moveDisks(int disks, int fromPile, int toPile);

public:
    int benchmark();

    bool verifyResult(int result) {
        return 8191 == result;
    }
};

#endif // _TOWERS_H
