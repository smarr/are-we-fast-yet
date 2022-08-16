# frozen_string_literal: true

# The Computer Language Benchmarks Game
# http://shootout.alioth.debian.org/
#
#     contributed by Mark C. Lewis
# modified slightly by Chad Whipkey
#
# Based on nbody.java ported to SOM and Ruby by Stefan Marr.

require_relative 'benchmark'

PI            = 3.141592653589793
SOLAR_MASS    = 4.0 * PI * PI
DAYS_PER_YEAR = 365.24

class NBody < Benchmark
  def inner_benchmark_loop(inner_iterations)
    system = NBodySystem.new

    inner_iterations.times { system.advance(0.01) }

    verify_result(system.energy, inner_iterations)
  end

  def verify_result(result, inner_iterations)
    return result == -0.1690859889909308  if inner_iterations == 250_000
    return result == -0.16907495402506745 if inner_iterations ==       1

    puts('No verification result for ' + inner_iterations.to_s + ' found')
    puts('Result is: ' + result.to_s)
    false
  end
end

class NBodySystem
  def initialize
    @bodies = create_bodies
  end

  def create_bodies
    bodies = [Body.sun,
              Body.jupiter,
              Body.saturn,
              Body.uranus,
              Body.neptune]

    px = py = pz = 0.0

    bodies.each do |b|
      px += b.vx * b.mass
      py += b.vy * b.mass
      pz += b.vz * b.mass
    end

    bodies[0].offset_momentum(px, py, pz)
    bodies
  end

  def advance(dt)
    @bodies.each_index do |i|
      i_body = @bodies[i]

      ((i + 1)...@bodies.size).each do |j|
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
      end
    end

    @bodies.each do |body|
      body.x += dt * body.vx
      body.y += dt * body.vy
      body.z += dt * body.vz
    end
  end

  def energy
    e = 0.0

    @bodies.each_index do |i|
      i_body = @bodies[i]

      e += 0.5 * i_body.mass *
          ((i_body.vx * i_body.vx) +
           (i_body.vy * i_body.vy) +
           (i_body.vz * i_body.vz))

      ((i + 1)...@bodies.size).each do |j|
        j_body = @bodies[j]

        dx = i_body.x - j_body.x
        dy = i_body.y - j_body.y
        dz = i_body.z - j_body.z

        distance = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz))
        e -= (i_body.mass * j_body.mass) / distance
      end
    end
    e
  end
end

class Body
  attr_accessor :x, :y, :z, :vx, :vy, :vz, :mass

  def initialize(x, y, z, vx, vy, vz, mass)
    @x = x
    @y = y
    @z = z
    @vx = vx * DAYS_PER_YEAR
    @vy = vy * DAYS_PER_YEAR
    @vz = vz * DAYS_PER_YEAR
    @mass = mass * SOLAR_MASS
  end

  def offset_momentum(px, py, pz)
    @vx = 0.0 - (px / SOLAR_MASS)
    @vy = 0.0 - (py / SOLAR_MASS)
    @vz = 0.0 - (pz / SOLAR_MASS)
  end

  def self.jupiter
    new(4.8414314424647209,
       -1.16032004402742839,
       -0.103622044471123109,
        0.00166007664274403694,
        0.00769901118419740425,
       -0.0000690460016972063023,
        0.000954791938424326609)
  end

  def self.saturn
    new(8.34336671824457987,
        4.12479856412430479,
       -0.403523417114321381,
       -0.00276742510726862411,
        0.00499852801234917238,
        0.0000230417297573763929,
        0.000285885980666130812)
  end

  def self.uranus
    new(12.894369562139131,
       -15.1111514016986312,
        -0.223307578892655734,
         0.00296460137564761618,
         0.0023784717395948095,
        -0.0000296589568540237556,
         0.0000436624404335156298)
  end

  def self.neptune
    new(15.3796971148509165,
       -25.9193146099879641,
         0.179258772950371181,
         0.00268067772490389322,
         0.00162824170038242295,
        -0.000095159225451971587,
         0.0000515138902046611451)
  end

  def self.sun
    new(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0)
  end
end
