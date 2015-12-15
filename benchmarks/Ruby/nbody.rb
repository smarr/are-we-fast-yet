# The Computer Language Benchmarks Game
# http://shootout.alioth.debian.org/
#
#     contributed by Mark C. Lewis
# modified slightly by Chad Whipkey
#
# Based on nbody.java ported to SOM by Stefan Marr.
PI            = 3.141592653589793
SOLAR_MASS    = 4.0 * PI * PI
DAYS_PER_YEAR = 365.24

class NBody < Benchmark
  def inner_benchmark_loop(inner_iterations)
    system = NBodySystem.new

    inner_iterations.times {
      system.advance(0.01)
    }

    verify_result(system.energy, inner_iterations)
  end

  def verify_result(result, inner_iterations)
    if inner_iterations == 250000
      return result == -0.1690859889909308
    end

    puts ('No verification result for ' + inner_iterations.to_s + ' found')
    puts ('Result is: ' + result.to_s)
    false
  end
end

class NBodySystem
  def initialize
    @bodies = create_bodies
  end

  def create_bodies
    bodies = Array.new(5)
    bodies[0] = Body.sun
    bodies[1] = Body.jupiter
    bodies[2] = Body.saturn
    bodies[3] = Body.uranus
    bodies[4] = Body.neptune

    px = py = pz = 0.0

    bodies.each { | b |
      px += b.vx * b.mass
      py += b.vy * b.mass
      pz += b.vz * b.mass
    }

    bodies[0].offset_momentum(px, py, pz)
    bodies
  end

  def advance(dt)
    @bodies.each_index { | i |
      i_body = @bodies[i]

      ((i + 1)..(@bodies.size - 1)).each { | j |
        j_body = @bodies[j]
        dx = i_body.x - j_body.x
        dy = i_body.y - j_body.y
        dz = i_body.z - j_body.z

        d_squared = (dx * dx) + (dy * dy) + (dz * dz)
        distance  = Math.sqrt(d_squared)
        mag       = dt / (d_squared * distance)

        i_body.vx -= dx * j_body.mass * mag
        i_body.vy -= dy * j_body.mass * mag
        i_body.vz -= dz * j_body.mass * mag

        j_body.vx += dx * i_body.mass * mag
        j_body.vy += dy * i_body.mass * mag
        j_body.vz += dz * i_body.mass * mag
      }
    }

    @bodies.each { | body |
      body.x += dt * body.vx
      body.y += dt * body.vy
      body.z += dt * body.vz
    }
  end

  def energy
    e = 0.0

    @bodies.each_index { | i |
      i_body = @bodies[i]

      e += 0.5 * i_body.mass *
          ((i_body.vx * i_body.vx) +
           (i_body.vy * i_body.vy) +
           (i_body.vz * i_body.vz))

      ((i + 1)..(@bodies.size - 1)).each { | j |
        j_body = @bodies[j]

        dx = i_body.x - j_body.x
        dy = i_body.y - j_body.y
        dz = i_body.z - j_body.z

        distance = Math.sqrt((dx*dx) + (dy*dy) + (dz*dz))
        e -= (i_body.mass * j_body.mass) / distance
      }
    }
    e
  end
end

class Body
  attr_accessor :x, :y, :z, :vx, :vy, :vz, :mass

  def initialize
    @x = @y = @z = @vx = @vy = @vz = @mass = 0.0
  end

  def offset_momentum(px, py, pz)
    @vx = 0.0 - (px / SOLAR_MASS)
    @vy = 0.0 - (py / SOLAR_MASS)
    @vz = 0.0 - (pz / SOLAR_MASS)
  end

  def self.jupiter
    b = self.new
    b.x  =   4.8414314424647209
    b.y  =  -1.16032004402742839
    b.z  =  -0.103622044471123109
    b.vx =   0.00166007664274403694   * DAYS_PER_YEAR
    b.vy =   0.00769901118419740425   * DAYS_PER_YEAR
    b.vz =  -0.0000690460016972063023 * DAYS_PER_YEAR
    b.mass = 0.000954791938424326609  * SOLAR_MASS
    b
  end

  def self.saturn
    b = self.new
    b.x    =  8.34336671824457987
    b.y    =  4.12479856412430479
    b.z    = -0.403523417114321381
    b.vx   = -0.00276742510726862411   * DAYS_PER_YEAR
    b.vy   =  0.00499852801234917238   * DAYS_PER_YEAR
    b.vz   =  0.0000230417297573763929 * DAYS_PER_YEAR
    b.mass =  0.000285885980666130812  * SOLAR_MASS
    b
  end

  def self.uranus
    b = self.new
    b.x    = 12.894369562139131
    b.y    =-15.1111514016986312
    b.z    = -0.223307578892655734
    b.vx   =  0.00296460137564761618   * DAYS_PER_YEAR
    b.vy   =  0.0023784717395948095    * DAYS_PER_YEAR
    b.vz   = -0.0000296589568540237556 * DAYS_PER_YEAR
    b.mass =  0.0000436624404335156298 * SOLAR_MASS
    b
  end

  def self.neptune
    b = self.new
    b.x    = 15.3796971148509165
    b.y    =-25.9193146099879641
    b.z    =  0.179258772950371181
    b.vx   =  0.00268067772490389322   * DAYS_PER_YEAR
    b.vy   =  0.00162824170038242295   * DAYS_PER_YEAR
    b.vz   = -0.000095159225451971587  * DAYS_PER_YEAR
    b.mass =  0.0000515138902046611451 * SOLAR_MASS
    b
  end

  def self.sun
    b = self.new
    b.mass = SOLAR_MASS
    b
  end
end
