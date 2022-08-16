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
require_relative 'som'

class Bounce < Benchmark
  def benchmark
    random = Random.new
    ball_count = 100
    bounces    = 0
    balls      = Array.new(ball_count) { Ball.new(random) }

    50.times do
      balls.each do |ball|
        bounces += 1 if ball.bounce
      end
    end
    bounces
  end

  def verify_result(result)
    result == 1331
  end
end

class Ball
  def initialize(random)
    @x = random.next % 500
    @y = random.next % 500

    @x_vel = (random.next % 300) - 150
    @y_vel = (random.next % 300) - 150
  end

  def bounce
    x_limit = y_limit = 500
    bounced = false

    @x += @x_vel
    @y += @y_vel

    if @x > x_limit
      @x = x_limit
      @x_vel = 0 - @x_vel.abs
      bounced = true
    end

    if @x < 0
      @x = 0
      @x_vel = @x_vel.abs
      bounced = true
    end

    if @y > y_limit
      @y = y_limit
      @y_vel = 0 - @y_vel.abs
      bounced = true
    end

    if @y < 0
      @y = 0
      @y_vel = @y_vel.abs
      bounced = true
    end
    bounced
  end
end
