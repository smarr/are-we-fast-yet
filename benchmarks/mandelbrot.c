// Copyright © 2004-2013 Brent Fulgham
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

// http://benchmarksgame.alioth.debian.org/u64q/program.php?test=mandelbrot&lang=yarv&id=3

#include <stdlib.h>
#include <stdio.h>

#include "harness.h"

int mandelbrot(int size) {
 int sum = 0;

 int byte_acc = 0;
 int bit_num = 0;

 int y = 0;
 while (y < size) {
   double ci = (2.0*y/size)-1.0;

   int x = 0;
   while (x < size) {
     double zr = 0.0;
     double zrzr = zr;
     double zi = 0.0;
     double zizi = zi;
     double cr = (2.0*x/size)-1.5;
     int escape = 1;

     int z = 0;
     while (z < 50) {
       double tr = zrzr - zizi + cr;
       double ti = 2.0*zr*zi + ci;
       zr = tr;
       zi = ti;
       // preserve recalculation
       zrzr = zr*zr;
       zizi = zi*zi;
       if (zrzr+zizi > 4.0) {
         escape = 0;
         break;
       }
       z += 1;
     }

     byte_acc = (byte_acc << 1) | escape;
     bit_num += 1;

     // Code is very similar for these cases, but using separate blocks
     // ensures we skip the shifting when it's unnecessary, which is most cases.
     if (bit_num == 8) {
       //print byte_acc.chr
       sum ^= byte_acc;
       byte_acc = 0;
       bit_num = 0;
     } else if (x == size - 1) {
       byte_acc <<= (8 - bit_num);
       //print byte_acc.chr
       sum ^= byte_acc;
       byte_acc = 0;
       bit_num = 0;
     }
     x += 1;
   }
   y += 1;
 }

 return sum;
}

int sample() {
  return mandelbrot(750) == 192;
}

int main(int argc, char** argv) {
  if (!sample()) {
     printf("Sanity check failed! Mandelbrot gives wrong result.\n");
     abort();
  }

  int iterations = 100;
  int warmup     = 0;
  int problem_size = 1000;

  parse_argv(argc, argv, &iterations, &warmup, &problem_size);

  volatile int result = 0; // to avoid mandelbrot() being optimized out

  while (iterations > 0) {
     unsigned long start = microseconds();
     result += mandelbrot(problem_size);
     unsigned long elapsed = microseconds() - start;
     printf("Mandelbrot: iterations=1 runtime: %lu%s\n", elapsed, "us");
     iterations--;
  }

  if (result == 0) {
    return 1;
  } else {
    return 0;
  }
}
