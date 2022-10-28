# frozen_string_literal: true

# Adapted based on SOM benchmark.
#
# Copyright (C) 2004-2013 Brent Fulgham
#
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
#   * Redistributions of source code must retain the above copyright notice,
#     this list of conditions and the following disclaimer.
#
#   * Redistributions in binary form must reproduce the above copyright notice,
#     this list of conditions and the following disclaimer in the documentation
#     and/or other materials provided with the distribution.
#
#   * Neither the name of "The Computer Language Benchmarks Game" nor the name
#     of "The Computer Language Shootout Benchmarks" nor the names of its
#     contributors may be used to endorse or promote products derived from this
#     software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

# The Computer Language Benchmarks Game
# http://benchmarksgame.alioth.debian.org
#
#  contributed by Karl von Laudermann
#  modified by Jeremy Echols
#  modified by Detlef Reichl
#  modified by Joseph LaFata
#  modified by Peter Zotov

# http://benchmarksgame.alioth.debian.org/u64q/program.php?test=mandelbrot&lang=yarv&id=3

require_relative 'benchmark'

class Mandelbrot < Benchmark
  def inner_benchmark_loop(inner_iterations)
    verify_result(mandelbrot(inner_iterations), inner_iterations)
  end

  def verify_result(result, inner_iterations)
    return result == 191 if inner_iterations == 500
    return result == 50  if inner_iterations == 750
    return result == 128 if inner_iterations ==   1

    puts('No verification result for ' + inner_iterations.to_s + ' found')
    puts('Result is: ' + result.to_s)
  end

  def mandelbrot(size)
    sum      = 0
    byte_acc = 0
    bit_num  = 0

    y = 0
    while y < size
      ci = (2.0 * y / size) - 1.0
      x  = 0

      while x < size
        zrzr = 0.0
        zizi = zi = 0.0
        cr   = (2.0 * x / size) - 1.5

        z = 0
        not_done = true
        escape = 0
        while not_done && z < 50
          zr = zrzr - zizi + cr
          zi = 2.0 * zr * zi + ci

          # preserve recalculation
          zrzr = zr * zr
          zizi = zi * zi
          if zrzr + zizi > 4.0
            not_done = false
            escape   = 1
          end
          z += 1
        end

        byte_acc = (byte_acc << 1) + escape
        bit_num += 1

        # Code is very similar for these cases, but using separate blocks
        # ensures we skip the shifting when it's unnecessary,
        # which is most cases.
        if bit_num == 8
          sum ^= byte_acc
          byte_acc = 0
          bit_num  = 0
        elsif x == size - 1
          byte_acc <<= (8 - bit_num)
          sum ^= byte_acc
          byte_acc = 0
          bit_num  = 0
        end
        x += 1
      end
      y += 1
    end

    sum
  end
end
