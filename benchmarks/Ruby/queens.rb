# frozen_string_literal: true

# This code is derived from the SOM benchmarks, see AUTHORS.md file.
#
# Copyright (c) 2015-2016 Stefan Marr <git@stefan-marr.de>
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the 'Software'), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.

require_relative 'benchmark'

class Queens < Benchmark
  def initialize
    @free_maxs  = nil
    @free_rows  = nil
    @free_mins  = nil
    @queen_rows = nil
  end

  def benchmark
    result = true
    10.times { result &&= queens }
    result
  end

  def verify_result(result)
    result
  end

  def queens
    @free_rows  = Array.new( 8, true)
    @free_maxs  = Array.new(16, true)
    @free_mins  = Array.new(16, true)
    @queen_rows = Array.new( 8, -1)

    place_queen(0)
  end

  def place_queen(c)
    8.times do |r|
      if get_row_column(r, c)
        @queen_rows[r] = c
        set_row_column(r, c, false)

        return true if c == 7
        return true if place_queen(c + 1)

        set_row_column(r, c, true)
      end
    end
    false
  end

  def get_row_column(r, c)
    @free_rows[r] && @free_maxs[c + r] && @free_mins[c - r + 7]
  end

  def set_row_column(r, c, v)
    @free_rows[r        ] = v
    @free_maxs[c + r    ] = v
    @free_mins[c - r + 7] = v
  end
end
