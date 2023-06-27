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

#include "Queens.h"

int Queens::benchmark()
{
    bool result = true;
    for (int i = 0; i < 10; i++) {
      result = result && queens();
    }
    return result;
}

bool Queens::queens()
{
    // stack allocation would save 2us of 165us, and further deviate from the original code.
    freeRows  = new bool[8];
    freeMaxs  = new bool[16];
    freeMins  = new bool[16];
    queenRows = new int[8];

    for( int i = 0; i < 8; i++ )
    {
        freeRows[i] = true;
        queenRows[i] = -1;
    }
    for( int i = 0; i < 16; i++ )
    {
        freeMaxs[i] = true;
        freeMins[i] = true;
    }

    const bool res = placeQueen(0);

    delete[] freeRows;
    delete[] freeMaxs;
    delete[] freeMins;
    delete[] queenRows;

    return res;
}

bool Queens::placeQueen(int c)
{
    for (int r = 0; r < 8; r++) {
      if (getRowColumn(r, c)) {
        queenRows[r] = c;
        setRowColumn(r, c, false);

        if (c == 7) {
          return true;
        }

        if (placeQueen(c + 1)) {
          return true;
        }
        setRowColumn(r, c, true);
      }
    }
    return false;
}

bool Queens::getRowColumn(int r, int c)
{
    return freeRows[r] && freeMaxs[c + r] && freeMins[c - r + 7];
}

void Queens::setRowColumn(int r, int c, bool v)
{
    freeRows[r        ] = v;
    freeMaxs[c + r    ] = v;
    freeMins[c - r + 7] = v;
}
