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

class List < Benchmark
  def benchmark
    result = tail(make_list(15),
                  make_list(10),
                  make_list(6))
    result.length
  end

  def make_list(length)
    if length == 0
      nil
    else
      e = Element.new(length)
      e.next = make_list(length - 1)
      e
    end
  end

  def is_shorter_than(x, y)
    x_tail = x
    y_tail = y

    while y_tail
      return true unless x_tail

      x_tail = x_tail.next
      y_tail = y_tail.next
    end
    false
  end

  def tail(x, y, z)
    if is_shorter_than(y, x)
      tail(tail(x.next, y, z),
           tail(y.next, z, x),
           tail(z.next, x, y))
    else
      z
    end
  end

  def verify_result(result)
    10 == result
  end
end

class Element
  attr_accessor :val, :next

  def initialize(v)
    @val  = v
    @next = nil
  end

  def length
    unless @next
      1
    else
      1 + @next.length
    end
  end
end
