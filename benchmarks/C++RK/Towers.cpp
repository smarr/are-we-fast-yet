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

#include "Towers.h"

void Towers::pushDisk(Towers::TowersDisk *disk, int pile)
{
    TowersDisk* top = piles[pile];
    if (!(top == 0) && (disk->getSize() >= top->getSize())) {
        throw "Cannot put a big disk on a smaller one";
    }

    disk->setNext(top);
    piles[pile] = disk;
}

Towers::TowersDisk *Towers::popDiskFrom(int pile)
{
    TowersDisk* top = piles[pile];
    if (top == 0) {
        throw "Attempting to remove a disk from an empty pile";
    }

    piles[pile] = top->getNext();
    top->setNext(0);
    return top;
}

void Towers::moveTopDisk(int fromPile, int toPile)
{
    pushDisk(popDiskFrom(fromPile), toPile);
    movesDone++;
}

void Towers::buildTowerAt(int pile, int disks)
{
    for (int i = disks; i >= 0; i--) {
        pushDisk(new TowersDisk(i), pile);
    }
}

void Towers::moveDisks(int disks, int fromPile, int toPile)
{
    if (disks == 1) {
        moveTopDisk(fromPile, toPile);
    } else {
        const int otherPile = (3 - fromPile) - toPile;
        moveDisks(disks - 1, fromPile, otherPile);
        moveTopDisk(fromPile, toPile);
        moveDisks(disks - 1, otherPile, toPile);
    }
}

int Towers::benchmark()
{
    for( int i = 0; i < PilesCount; i++ )
        piles[i] = 0;
    buildTowerAt(0, 13);
    movesDone = 0;
    moveDisks(13, 0, 1);
    for( int i = 0; i < PilesCount; i++ )
        if( piles[i] )
            delete piles[i];
    return movesDone;
}
