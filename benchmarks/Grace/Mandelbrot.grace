//* This version is a transcription of the Ruby implementation mandelbrot.rb
// found with JRuby
// https://raw.githubusercontent.com/jruby/jruby/3e43676ee6dc3c13e70fe4a52cce685128c23b8e/bench/truffle/mandelbrot.rb
//   The original copyright statement reads as follows:
//   Copyright © 2004-2013 Brent Fulgham
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
//   * Neither the name of 'The Computer Language Benchmarks Game' nor the name
//     of 'The Computer Language Shootout Benchmarks' nor the names of its
//     contributors may be used to endorse or promote products derived from this
//     software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 'AS IS'
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The Computer Language Benchmarks Game
// http://benchmarksgame.alioth.debian.org
//
//  contributed by Karl von Laudermann
//  modified by Jeremy Echols
//  modified by Detlef Reichl
//  modified by Joseph LaFata
//  modified by Peter Zotov
//
// http://benchmarksgame.alioth.debian.org/u64q/program.php?test=mandelbrot&lang=yarv&id=3
//
//
// Adapted for Grace by Richard Roberts
//   2018, June
//

var firstResult

method mandelbrot (size) {
  var sum := 0.asInteger
  var byteAcc := 0.asInteger
  var bitNum := 0.asInteger

  var y := 0.asInteger
  { y < size }.whileTrue {
    var ci := (2 * y / (size + 0)) - 1
    var x := 0.asInteger

    { x < size }.whileTrue {
      var zr
      var zrzr
      var zi
      var zizi
      var cr
      var escape
      var z
      var notDone

      zrzr := 0
      zr   := 0
      zizi := 0
      zi   := 0
      cr   := (2 * x / (size + 0)) - 1.5

      z := 0.asInteger
      notDone := true
      escape := 0.asInteger
      { notDone.and {z < 50.asInteger} }.whileTrue {
        zr := zrzr - zizi + cr
        zi := 2 * zr * zi + ci

        // preserve recalculation
        zrzr := zr * zr
        zizi := zi * zi

        ((zrzr + zizi) > 4).ifTrue {
          notDone := false
          escape := 1.asInteger
        }
        z := z + 1.asInteger
      }

      byteAcc := (byteAcc.bitLeftShift(1.asInteger)) + escape
      bitNum := bitNum + 1.asInteger

      // Code is very similar for these cases, but using separate
      // blocks ensures we skip the shifting when it is unnecessary,
      // which is in most cases.
      (bitNum == 8.asInteger).ifTrue {
        sum       := sum.bitXor(byteAcc)
        byteAcc   := 0.asInteger
        bitNum    := 0.asInteger
      } ifFalse {
        (x == (size - 1.asInteger)).ifTrue {
          byteAcc := byteAcc << (8.asInteger - bitNum)
          sum     := sum.bitXor(byteAcc)
          byteAcc := 0.asInteger
          bitNum  := 0.asInteger
        }
      }

      x := x + 1.asInteger
    }

    y := y + 1.asInteger
  }

  sum
}

method asString {"Mandelbrot.grace"}

method benchmark(innerIterations) {
  def result = mandelbrot(innerIterations)
  firstResult.isNil.ifTrue {
    firstResult := result
  }
  result == firstResult
}
