# frozen_string_literal: true

# Ported from the adapted JavaScript and Java versions.
#
#     Copyright (c) 2001-2010, Purdue University. All rights reserved.
#     Copyright (C) 2015 Apple Inc. All rights reserved.
#
#     Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
# * Redistributions of source code must retain the above copyright
# notice, this list of conditions and the following disclaimer.
#         * Redistributions in binary form must reproduce the above copyright
# notice, this list of conditions and the following disclaimer in the
# documentation and/or other materials provided with the distribution.
#  * Neither the name of the Purdue University nor the
#    names of its contributors may be used to endorse or promote products
#    derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

require_relative 'benchmark'
require_relative 'som'

## PREFER == nil over .nil?
## PREFER or just if x (wihtout the !=nil)

MIN_X = 0.0
MIN_Y = 0.0
MAX_X = 1000.0
MAX_Y = 1000.0
MIN_Z = 0.0
MAX_Z = 10.0
PROXIMITY_RADIUS = 1.0
GOOD_VOXEL_SIZE  = PROXIMITY_RADIUS * 2.0

class Vector2D
  attr_reader :x, :y

  def initialize(x, y)
    @x = x
    @y = y
  end

  def plus(other)
    Vector2D.new(@x + other.x, @y + other.y)
  end

  def minus(other)
    Vector2D.new(@x - other.x, @y - other.y)
  end

  def compare_to(other)
    result = compare_numbers(@x, other.x)
    return result unless result == 0

    compare_numbers(@y, other.y)
  end

  def compare_numbers(a, b)
    return  0 if a == b
    return -1 if a < b
    return  1 if a > b

    # We say that NaN is smaller than non-NaN.
    return 1 if a == a
    -1
  end
end

class Vector3D
  attr_reader :x, :y, :z

  def initialize(x, y, z)
    @x = x
    @y = y
    @z = z
  end

  def plus(other)
    Vector3D.new(@x + other.x, @y + other.y, @z + other.z)
  end

  def minus(other)
    Vector3D.new(@x - other.x, @y - other.y, @z - other.z)
  end

  def dot(other)
    @x * other.x + @y * other.y + @z * other.z
  end

  def squared_magnitude
    dot(self)
  end

  def magnitude
    Math.sqrt(squared_magnitude)
  end

  def times(amount)
    Vector3D.new(@x * amount, @y * amount, @z * amount)
  end
end

HORIZONTAL = Vector2D.new(GOOD_VOXEL_SIZE, 0.0)
VERTICAL   = Vector2D.new(0.0, GOOD_VOXEL_SIZE)

class Node
  attr_reader :key
  attr_accessor :value, :left, :right, :parent, :color

  def initialize(key, value)
    @key    = key
    @value  = value
    @left   = nil
    @right  = nil
    @parent = nil
    @color  = :red
  end

  def successor
    x = self
    return tree_minimum(x.right) if x.right

    y = x.parent
    while y && x == y.right
      x = y
      y = y.parent
    end
    y
  end
end

class RbtEntry
  attr_reader :key, :value

  def initialize(key, value)
    @key   = key
    @value = value
  end
end

class InsertResult
  attr_reader :is_new_entry, :new_node, :old_value

  def initialize(is_new_entry, new_node, old_value)
    @is_new_entry = is_new_entry
    @new_node     = new_node
    @old_value    = old_value
  end
end

def tree_minimum(x)
  current = x
  current = current.left while current.left

  current
end

class RedBlackTree
  def initialize
    @root = nil
  end

  def put(key, value)
    insertion_result = tree_insert(key, value)
    return insertion_result.old_value unless insertion_result.is_new_entry

    x = insertion_result.new_node

    while !x.equal?(@root) && x.parent.color == :red
      if x.parent.equal? x.parent.parent.left
        y = x.parent.parent.right
        if y && y.color == :red
          # Case 1
          x.parent.color = :black
          y.color = :black
          x.parent.parent.color = :red
          x = x.parent.parent
        else
          if x.equal? x.parent.right
            # Case 2
            x = x.parent
            left_rotate(x)
          end

          # Case 3
          x.parent.color = :black
          x.parent.parent.color = :red
          right_rotate(x.parent.parent)
        end
      else
        # Same as "then" clause with "right" and "left" exchanged.
        y = x.parent.parent.left
        if y && y.color == :red
          # Case 1
          x.parent.color = :black
          y.color = :black
          x.parent.parent.color = :red
          x = x.parent.parent
        else
          if x.equal? x.parent.left
            # Case 2
            x = x.parent
            right_rotate(x)
          end

          # Case 3
          x.parent.color = :black
          x.parent.parent.color = :red
          left_rotate(x.parent.parent)
        end
      end
    end

    @root.color = :black
    nil
  end

  def remove(key)
    z = find_node(key)

    return nil unless z

    # Y is the node to be unlinked from the tree.
    if !z.left || !z.right
      y = z
    else
      y = z.successor
    end

    # Y is guaranteed to be non-null at this point.
    if y.left
      x = y.left
    else
      x = y.right
    end

    # X is the child of y which might potentially replace y
    # in the tree. X might be null at this point.
    if x
      x.parent = y.parent
      x_parent = x.parent
    else
      x_parent = y.parent
    end

    unless y.parent
      @root = x
    else
      if y.equal?(y.parent.left)
        y.parent.left = x
      else
        y.parent.right = x
      end
    end

    if !y.equal?(z)
      remove_fixup(x, x_parent) if y.color == :black

      y.parent = z.parent
      y.color  = z.color
      y.left   = z.left
      y.right  = z.right

      z.left.parent = y if z.left
      z.right.parent = y if z.right
      if z.parent
        if z.parent.left == z
          z.parent.left = y
        else
          z.parent.right = y
        end
      else
        @root = y
      end
    elsif y.color == :black
      remove_fixup(x, x_parent)
    end

    z.value
  end

  def get(key)
    node = find_node(key)
    return nil unless node

    node.value
  end

  def for_each
    return unless @root

    current = tree_minimum(@root)

    while current
      yield RbtEntry.new(current.key, current.value)
      current = current.successor
    end
  end

  def find_node(key)
    current = @root
    while current
      comparison_result = key.compare_to(current.key)
      return current if comparison_result == 0

      if comparison_result < 0
        current = current.left
      else
        current = current.right
      end
    end
    nil
  end

  def tree_insert(key, value)
    y = nil
    x = @root

    while x
      y = x
      comparison_result = key.compare_to(x.key)
      if comparison_result < 0
        x = x.left
      elsif comparison_result > 0
        x = x.right
      else
        old_value = x.value
        x.value = value
        return InsertResult.new(false, nil, old_value)
      end
    end

    z = Node.new(key, value)
    z.parent = y

    unless y
      @root = z
    else
      if key.compare_to(y.key) < 0
        y.left = z
      else
        y.right = z
      end
    end
    InsertResult.new(true, z, nil)
  end

  def left_rotate(x)
    y = x.right

    # Turn y's left subtree into x's right subtree.
    x.right = y.left
    y.left.parent = x if y.left

    # Link x's parent to y.
    y.parent = x.parent
    unless x.parent
      @root = y
    else
      if x.equal? x.parent.left
        x.parent.left = y
      else
        x.parent.right = y
      end
    end

    # Put x on y's left.
    y.left   = x
    x.parent = y

    y
  end

  def right_rotate(y)
    x = y.left

    # Turn x's right subtree into y's left subtree.
    y.left = x.right
    x.right.parent = y if x.right

    # Link y's parent to x.
    x.parent = y.parent
    unless y.parent
      @root = x
    else
      if y.equal? y.parent.left
        y.parent.left = x
      else
        y.parent.right = x
      end
    end

    x.right = y
    y.parent = x

    x
  end

  def remove_fixup(x, x_parent)
    while !x.equal?(@root) && (!x || x.color == :black)
      if x.equal? x_parent.left
        # Note: the text points out that w cannot be null.
        # The reason is not obvious from simply looking at the code;
        # it comes about from the properties of the red-black tree.
        w = x_parent.right
        if w.color == :red
          # Case 1
          w.color = :black
          x_parent.color = :red
          left_rotate(x_parent)
          w = x_parent.right
        end
        if (!w.left || w.left.color == :black) &&
           (!w.right || w.right.color == :black)
          # Case 2
          w.color = :red
          x = x_parent
          x_parent = x.parent
        else
          if !w.right || w.right.color == :black
            # Case 3
            w.left.color = :black
            w.color = :red
            right_rotate(w)
            w = x_parent.right
          end
          # Case 4
          w.color = x_parent.color
          x_parent.color = :black
          w.right.color = :black if w.right
          left_rotate(x_parent)
          x = @root
          x_parent = x.parent
        end
      else
        # Same as "then" clause with "right" and "left" exchanged.
        w = x_parent.left
        if w.color == :red
          # Case 1
          w.color = :black
          x_parent.color = :red
          right_rotate(x_parent)
          w = x_parent.left
        end
        if (!w.right || w.right.color == :black) &&
           (!w.left || w.left.color == :black)
          # Case 2
          w.color = :red
          x = x_parent
          x_parent = x.parent
        else
          if !w.left || w.left.color == :black
            # Case 3
            w.right.color = :black
            w.color = :red
            left_rotate(w)
            w = x_parent.left
          end
          # Case 4
          w.color = x_parent.color
          x_parent.color = :black
          w.left.color = :black if w.left
          right_rotate(x_parent)
          x = @root
          x_parent = x.parent
        end
      end
    end
    x.color = :black if x
  end
end

class CallSign
  attr_reader :value

  def initialize(value)
    @value = value
  end

  def compare_to(other)
    value == other.value ? 0 : (value < other.value ? -1 : 1)
  end
end

class Collision
  attr_reader :aircraft_a, :aircraft_b, :position

  def initialize(aircraft_a, aircraft_b, position)
    @aircraft_a = aircraft_a
    @aircraft_b = aircraft_b
    @position  = position
  end
end

class CollisionDetector
  def initialize
    @state = RedBlackTree.new
  end

  def handle_new_frame(frame)
    motions = Vector.new
    seen    = RedBlackTree.new

    frame.each do |aircraft|
      old_position = @state.put(aircraft.callsign, aircraft.position)
      new_position = aircraft.position
      seen.put(aircraft.callsign, true)

      unless old_position
        # Treat newly introduced aircraft as if they were stationary.
        old_position = new_position
      end

      motions.append(Motion.new(aircraft.callsign, old_position, new_position))
    end

    # Remove aircraft that are no longer present.
    to_remove = Vector.new
    @state.for_each do |e|
      to_remove.append(e.key) unless seen.get(e.key)
    end

    to_remove.each { |e| @state.remove(e) }

    all_reduced = reduce_collision_set(motions)
    collisions = Vector.new
    all_reduced.each do |reduced|
      (0...reduced.size).each do |i|
        motion1 = reduced.at(i)
        ((i + 1)...reduced.size).each do |j|
          motion2 = reduced.at(j)
          collision = motion1.find_intersection(motion2)
          if collision
            collisions.append(Collision.new(motion1.callsign,
                                            motion2.callsign,
                                            collision))
          end
        end
      end
    end

    collisions
  end

  def is_in_voxel(voxel, motion)
    if voxel.x > MAX_X ||
       voxel.x < MIN_X ||
       voxel.y > MAX_Y ||
       voxel.y < MIN_Y
      return false
    end

    init = motion.pos_one
    fin  = motion.pos_two

    v_s = GOOD_VOXEL_SIZE
    r   = PROXIMITY_RADIUS / 2.0

    v_x = voxel.x
    x0 = init.x
    xv = fin.x - init.x

    v_y = voxel.y
    y0 = init.y
    yv = fin.y - init.y

    low_x  = (v_x - r - x0) / xv
    high_x = (v_x + v_s + r - x0) / xv

    if xv < 0.0
      tmp = low_x
      low_x = high_x
      high_x = tmp
    end

    low_y  = (v_y - r - y0) / yv
    high_y = (v_y + v_s + r - y0) / yv

    if yv < 0.0
      tmp = low_y
      low_y = high_y
      high_y = tmp
    end

    (((xv == 0.0 && v_x <= x0 + r && x0 - r <= v_x + v_s) || # no motion in x
     (low_x <= 1.0 && 1.0 <= high_x) || (low_x <= 0.0 && 0.0 <= high_x) ||
      (0.0 <= low_x && high_x <= 1.0)) &&
    ((yv == 0.0 && v_y <= y0 + r && y0 - r <= v_y + v_s) || # no motion in y
     ((low_y <= 1.0 && 1.0 <= high_y) || (low_y <= 0.0 && 0.0 <= high_y) ||
      (0.0 <= low_y && high_y <= 1.0))) &&
    (xv == 0.0 || yv == 0.0 || # no motion in x or y or both
     (low_y <= high_x && high_x <= high_y) ||
     (low_y <= low_x && low_x <= high_y) ||
     (low_x <= low_y && high_y <= high_x)))
  end

  def put_into_map(voxel_map, voxel, motion)
    array = voxel_map.get(voxel)
    unless array
      array = Vector.new
      voxel_map.put(voxel, array)
    end
    array.append(motion)
  end

  def recurse(voxel_map, seen, next_voxel, motion)
    return unless is_in_voxel(next_voxel, motion)
    return if seen.put(next_voxel, true)

    put_into_map(voxel_map, next_voxel, motion)

    recurse(voxel_map, seen, next_voxel.minus(HORIZONTAL), motion)
    recurse(voxel_map, seen, next_voxel.plus(HORIZONTAL),  motion)
    recurse(voxel_map, seen, next_voxel.minus(VERTICAL),   motion)
    recurse(voxel_map, seen, next_voxel.plus(VERTICAL),    motion)
    recurse(voxel_map, seen, next_voxel.minus(HORIZONTAL).minus(VERTICAL), motion)
    recurse(voxel_map, seen, next_voxel.minus(HORIZONTAL).plus(VERTICAL),  motion)
    recurse(voxel_map, seen, next_voxel.plus(HORIZONTAL).minus(VERTICAL),  motion)
    recurse(voxel_map, seen, next_voxel.plus(HORIZONTAL).plus(VERTICAL),   motion)
  end

  def reduce_collision_set(motions)
    voxel_map = RedBlackTree.new
    motions.each { |motion| draw_motion_on_voxel_map(voxel_map, motion) }

    result = Vector.new
    voxel_map.for_each do |e|
      result.append(e.value) if e.value.size > 1
    end
    result
  end

  def voxel_hash(position)
    x_div = (position.x / GOOD_VOXEL_SIZE).to_i
    y_div = (position.y / GOOD_VOXEL_SIZE).to_i

    x = GOOD_VOXEL_SIZE * x_div
    y = GOOD_VOXEL_SIZE * y_div

    x -= GOOD_VOXEL_SIZE if position.x < 0.0
    y -= GOOD_VOXEL_SIZE if position.y < 0.0

    Vector2D.new(x, y)
  end

  def draw_motion_on_voxel_map(voxel_map, motion)
    seen = RedBlackTree.new
    recurse(voxel_map, seen, voxel_hash(motion.pos_one), motion)
  end
end

class Motion
  attr_reader :callsign, :pos_one, :pos_two

  def initialize(callsign, pos_one, pos_two)
    @callsign = callsign
    @pos_one = pos_one
    @pos_two = pos_two
  end

  def delta
    @pos_two.minus(@pos_one)
  end

  def find_intersection(other)
    init1 = @pos_one
    init2 = other.pos_one
    vec1 = delta
    vec2 = other.delta
    radius = PROXIMITY_RADIUS

    # this test is not geometrical 3-d intersection test,
    # it takes the fact that the aircraft move
    # into account; so it is more like a 4d test
    # (it assumes that both of the aircraft have a constant speed
    # over the tested interval)

    # we thus have two points,
    # each of them moving on its line segment at constant speed;
    # we are looking for times when the distance between
    # these two points is smaller than r

    # vec1 is vector of aircraft 1
    # vec2 is vector of aircraft 2

    # a = (V2 - V1)^T * (V2 - V1)
    a = vec2.minus(vec1).squared_magnitude

    if a != 0.0
      # we are first looking for instances
      # of time when the planes are exactly r from each other
      # at least one plane is moving;
      # if the planes are moving in parallel, they do not have constant speed

      # if the planes are moving in parallel, then
      #   if the faster starts behind the slower,
      #     we can have 2, 1, or 0 solutions
      #   if the faster plane starts in front of the slower,
      #     we can have 0 or 1 solutions

      # if the planes are not moving in parallel, then

      # point P1 = I1 + vV1
      # point P2 = I2 + vV2
      #   - looking for v, such that dist(P1,P2) = || P1 - P2 || = r

      # it follows that || P1 - P2 || = sqrt( < P1-P2, P1-P2 > )
      #   0 = -r^2 + < P1 - P2, P1 - P2 >
      #  from properties of dot product
      #   0 = -r^2 + <I1-I2,I1-I2> + v * 2<I1-I2, V1-V2> + v^2 *<V1-V2,V1-V2>
      #   so we calculate a, b, c - and solve the quadratic equation
      #   0 = c + bv + av^2

      # b = 2 * <I1-I2, V1-V2>
      b = 2.0 * init1.minus(init2).dot(vec1.minus(vec2))

      # c = -r^2 + (I2 - I1)^T * (I2 - I1)
      c = -radius * radius + init2.minus(init1).squared_magnitude

      discr = b * b - 4.0 * a * c
      return nil if discr < 0.0

      v1 = (-b - Math.sqrt(discr)) / (2.0 * a)
      v2 = (-b + Math.sqrt(discr)) / (2.0 * a)

      if v1 <= v2 && ((v1  <= 1.0 && 1.0 <= v2) ||
          (v1  <= 0.0 && 0.0 <= v2) ||
          (0.0 <= v1  && v2  <= 1.0))
        # Pick a good "time" at which to report the collision.
        if v1 <= 0.0
          # The collision started before this frame.
          # Report it at the start of the frame.
          v = 0.0
        else
          # The collision started during this frame. Report it at that moment.
          v = v1
        end

        result1 = init1.plus(vec1.times(v))
        result2 = init2.plus(vec2.times(v))

        result = result1.plus(result2).times(0.5)
        if result.x >= MIN_X &&
           result.x <= MAX_X &&
           result.y >= MIN_Y &&
           result.y <= MAX_Y &&
           result.z >= MIN_Z &&
           result.z <= MAX_Z
          return result
        end
      end

      return nil
    end

    # the planes have the same speeds and are moving in parallel
    # (or they are not moving at all)
    # they  thus have the same distance all the time;
    # we calculate it from the initial point

    # dist = || i2 - i1 || = sqrt(  ( i2 - i1 )^T * ( i2 - i1 ) )
    dist = init2.minus(init1).magnitude
    return init1.plus(init2).times(0.5) if dist <= radius

    nil
  end
end

class Aircraft
  attr_reader :callsign, :position

  def initialize(callsign, position)
    @callsign = callsign
    @position = position
  end
end

class Simulator
  def initialize(num_aircrafts)
    @aircraft = Vector.new
    (0...num_aircrafts).each do |i|
      @aircraft.append(CallSign.new(i))
    end
  end

  def simulate(time)
    frame = Vector.new
    (0...@aircraft.size).step(2) do |i|
      frame.append(Aircraft.new(@aircraft.at(i),
                                Vector3D.new(time,
                                             Math.cos(time) * 2.0 + i * 3.0, 10.0)))
      frame.append(Aircraft.new(@aircraft.at(i + 1),
                                Vector3D.new(time,
                                             Math.sin(time) * 2.0 + i * 3.0, 10.0)))
    end
    frame
  end
end

class CD < Benchmark
  def benchmark(num_aircrafts)
    num_frames = 200
    simulator  = Simulator.new(num_aircrafts)
    detector   = CollisionDetector.new

    actual_collisions = 0

    (0...num_frames).each do |i|
      time = i / 10.0
      collisions = detector.handle_new_frame(simulator.simulate(time))
      actual_collisions += collisions.size
    end

    actual_collisions
  end

  def inner_benchmark_loop(inner_iterations)
    verify_result(benchmark(inner_iterations), inner_iterations)
  end

  def verify_result(actual_collisions, num_aircrafts)
    return actual_collisions == 14_484 if num_aircrafts == 1000
    return actual_collisions == 14_484 if num_aircrafts ==  500
    return actual_collisions == 10_830 if num_aircrafts ==  250
    return actual_collisions ==  8_655 if num_aircrafts ==  200
    return actual_collisions ==  4_305 if num_aircrafts ==  100
    return actual_collisions ==    390 if num_aircrafts ==   10
    return actual_collisions ==     42 if num_aircrafts ==    2

    puts('No verification result for ' + num_aircrafts.to_s + ' found')
    puts('Result is: ' + actual_collisions.to_s)
    false
  end
end
