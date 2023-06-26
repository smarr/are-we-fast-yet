/* This code is derived from the SOM benchmarks, see AUTHORS.md file.
 *
 * Copyright (c) 2023 Rochus Keller <me@rochus-keller.ch> (for C++ migration)
 *
// Copyright (C) 2004-2013 Brent Fulgham
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
//   * Redistributions of source code must retain the above copyright notice,
//     this list of conditions and the following disclaimer.
//
//   * Redistributions in binary form must reproduce the above copyright notice,
//     this list of conditions and the following disclaimer in the documentation
//     and/or other materials provided with the distribution.
//
//   * Neither the name of "The Computer Language Benchmarks Game" nor the name
//     of "The Computer Language Shootout Benchmarks" nor the names of its
//     contributors may be used to endorse or promote products derived from this
//     software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

// The Computer Language Benchmarks Game
// http://benchmarksgame.alioth.debian.org
//
//  contributed by Karl von Laudermann
//  modified by Jeremy Echols
//  modified by Detlef Reichl
//  modified by Joseph LaFata
//  modified by Peter Zotov
*/

#include "Mandelbrot.h"
#include <iostream>

bool Mandelbrot::verifyResult(int result, int innerIterations)
{
    if (innerIterations == 500) {
        return result == 191;
    }
    if (innerIterations == 750) {
        return result == 50;
    }
    if (innerIterations == 1) {
        return result == 128;
    }

    // Checkstyle: stop
    std::cerr << "No verification result for " << innerIterations << " found" << std::endl;
    std::cerr << "Result is: " << result << std::endl;
    // Checkstyle: resume
    return false;
}

int Mandelbrot::mandelbrot(int size)
{
    int sum     = 0;
    int byteAcc = 0;
    int bitNum  = 0;

    int y = 0;

    while (y < size) {
        double ci = (2.0 * y / size) - 1.0;
        int x = 0;

        while (x < size) {
            double zr   = 0.0;
            double zrzr = 0.0;
            double zi   = 0.0;
            double zizi = 0.0;
            double cr = (2.0 * x / size) - 1.5;

            int z = 0;
            bool notDone = true;
            int escape = 0;
            while (notDone && z < 50) {
                zr = zrzr - zizi + cr;
                zi = 2.0 * zr * zi + ci;

                // preserve recalculation
                zrzr = zr * zr;
                zizi = zi * zi;

                if (zrzr + zizi > 4.0) {
                    notDone = false;
                    escape  = 1;
                }
                z += 1;
            }

            byteAcc = (byteAcc << 1) + escape;
            bitNum += 1;

            // Code is very similar for these cases, but using separate blocks
            // ensures we skip the shifting when it's unnecessary, which is most cases.
            if (bitNum == 8) {
                sum ^= byteAcc;
                byteAcc = 0;
                bitNum  = 0;
            } else if (x == size - 1) {
                byteAcc <<= (8 - bitNum);
                sum ^= byteAcc;
                byteAcc = 0;
                bitNum  = 0;
            }
            x += 1;
        }
        y += 1;
    }
    return sum;
}
