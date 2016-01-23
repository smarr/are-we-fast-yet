require "benchmarkx"
require "som"

class Bounce < BenchmarkX
  def benchmark
    random = SomRandom.new
    ball_count = 100
    bounces    = 0
    balls      = Array.new(ball_count) { Ball.new(random) }

    50.times {
      balls.each { | ball |
        if ball.bounce
          bounces += 1
        end
      }
    }
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
      @x = x_limit; @x_vel = 0 - @x_vel.abs; bounced = true
    end

    if @x < 0
      @x = 0;       @x_vel = @x_vel.abs;     bounced = true
    end

    if @y > y_limit
      @y = y_limit; @y_vel = 0 - @y_vel.abs; bounced = true
    end

    if @y < 0
      @y = 0;       @y_vel = @y_vel.abs;     bounced = true
    end
    bounced
  end
end

TheBenchmark = Bounce

require "harness"
