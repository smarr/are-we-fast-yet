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
require "./benchmark"
require "./som"

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
  getter :x, :y

  def initialize(x : Float64, y : Float64)
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
    unless result == 0
      return result
    end
    compare_numbers(@y, other.y)
  end

  def compare_numbers(a, b)
    if a == b
      return 0
    end
    if a < b
      return -1
    end
    if a > b
      return 1
    end

    # We say that NaN is smaller than non-NaN.
    if a == a
      return 1
    end
    -1
  end

  def to_s
    "V#{x}:#{y}"
  end
end

class Vector3D
  getter :x, :y, :z

  def initialize(x : Float64, y : Float64, z : Float64)
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

  def to_s
    "V#{@x}:#{@y}:#{z}"
  end
end

HORIZONTAL = Vector2D.new(GOOD_VOXEL_SIZE, 0.0)
VERTICAL   = Vector2D.new(0.0, GOOD_VOXEL_SIZE)

class Node(K, V)
  getter :key
  property :value, :left, :right, :parent, :color
  
  @left   : Node(K, V)?
  @right  : Node(K, V)?
  @parent : Node(K, V)?

  def initialize(key : K, value : V)
    @key    = key
    @value  = value
    @left   = nil
    @right  = nil
    @parent = nil
    @color  = :red
  end

  def successor
    x = self
    if x.right
      return tree_minimum(x.right.not_nil!)
    end

    y = x.parent
    while y && x == y.right
      x = y
      y = y.parent
    end
    y
  end
end

class RbtEntry(K, V)
  getter :key, :value

  def initialize(key : K, value : V)
    @key   = key
    @value = value
  end
end

class InsertResult(K, V)
  getter :is_new_entry, :new_node, :old_value

  def initialize(is_new_entry : Bool, new_node : Node(K, V)?, old_value : V?)
    @is_new_entry = is_new_entry
    @new_node     = new_node
    @old_value    = old_value
  end
end

def tree_minimum(x : Node(K, V)) forall K, V
  current = x
  while current.left
    current = current.left.not_nil!
  end
  current
end

class RedBlackTree(K, V)
  @root : Node(K, V)?

  def initialize
    @root = nil
  end

  def put(key : K, value : V)
    insertion_result = tree_insert(key, value)
    unless insertion_result.is_new_entry
      return insertion_result.old_value
    end

    x = insertion_result.new_node

    while x && x != @root && x.parent.not_nil!.color == :red
      if x.parent == x.parent.not_nil!.parent.not_nil!.left
        y = x.parent.not_nil!.parent.not_nil!.right
        if y && y.color == :red
          # Case 1
          x.parent.not_nil!.color = :black
          y.color = :black
          x.parent.not_nil!.parent.not_nil!.color = :red
          x = x.parent.not_nil!.parent.not_nil!
        else
          if x == x.parent.not_nil!.right
            # Case 2
            x = x.parent.not_nil!
            left_rotate(x.not_nil!)
          end

          # Case 3
          x.parent.not_nil!.color = :black
          x.parent.not_nil!.parent.not_nil!.color = :red
          right_rotate(x.parent.not_nil!.parent.not_nil!)
        end
      else
        # Same as "then" clause with "right" and "left" exchanged.
        y = x.parent.not_nil!.parent.not_nil!.left
        if y && y.color == :red
           # Case 1
           x.parent.not_nil!.color = :black
           y.color = :black
           x.parent.not_nil!.parent.not_nil!.color = :red
           x = x.parent.not_nil!.parent
        else
          if x == x.parent.not_nil!.left
            # Case 2
            x = x.parent.not_nil!
            right_rotate(x)
          end

          # Case 3
          x.parent.not_nil!.color = :black
          x.parent.not_nil!.parent.not_nil!.color = :red
          left_rotate(x.parent.not_nil!.parent.not_nil!)
        end
      end
    end

    @root.not_nil!.color = :black
    nil
  end

  def remove(key : K)
    z = find_node(key)

    if !z
      return nil
    end

    # Y is the node to be unlinked from the tree.
    if !z.left || !z.right
      y = z
    else
      y = z.successor.not_nil!
    end

    # Y is guaranteed to be non-null at this point.
    if y.left
      x = y.left
    else
      x = y.right
    end

    # X is the child of y which might potentially replace y in the tree. X might be null at this point.
    if x
      x.parent = y.parent
      x_parent = x.parent.not_nil!
    else
      x_parent = y.parent.not_nil!
    end

    if y.parent
      if y == y.parent.not_nil!.left
        y.parent.not_nil!.left = x
      else
        y.parent.not_nil!.right = x
      end
    else
      @root = x
    end

    if y != z
      if y.color == :black
        remove_fixup(x, x_parent)
      end

      y.parent = z.parent
      y.color  = z.color
      y.left   = z.left
      y.right  = z.right

      if z.left
        z.left.not_nil!.parent = y
      end
      if z.right
        z.right.not_nil!.parent = y
      end
      if z.parent
        if z.parent.not_nil!.left == z
          z.parent.not_nil!.left = y
        else
          z.parent.not_nil!.right = y
        end
      else
        @root = y
      end
    elsif y.color == :black
      remove_fixup(x, x_parent)
    end

    z.value
  end

  def get(key : K)
    node = find_node(key)
    if !node
      return nil
    end
    node.value
  end

  def for_each ## &block
    unless @root
      return
    end

    current = tree_minimum(@root.not_nil!)

    while current
      yield RbtEntry(K, V).new(current.key, current.value)
      current = current.successor
    end
  end

  def find_node(key : K)
    current = @root
    while current
      comparison_result = key.compare_to(current.key)
      if comparison_result == 0
        return current
      end

      if comparison_result < 0
        current = current.left
      else
        current = current.right
      end
    end
    nil
  end

  def tree_insert(key : K, value : V)
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
        return InsertResult(K, V).new(false, nil, old_value)
      end
    end

    z = Node.new(key, value)
    z.parent = y

    if y
      if key.compare_to(y.key) < 0
        y.left = z
      else
        y.right = z
      end
    else
      @root = z
    end
    InsertResult(K, V).new(true, z, nil)
  end

  def left_rotate(x : Node(K, V))
    y = x.right.not_nil!

    # Turn y's left subtree into x's right subtree.
    x.right = y.left
    if y.left
      y.left.not_nil!.parent = x
    end

    # Link x's parent to y.
    y.parent = x.parent
    if x.parent
      if x == x.parent.not_nil!.left
        x.parent.not_nil!.left = y
      else
        x.parent.not_nil!.right = y
      end
    else
      @root = y
    end

    # Put x on y's left.
    y.left   = x
    x.parent = y

    y
  end

  def right_rotate(y : Node(K, V))
    x = y.left.not_nil!

    # Turn x's right subtree into y's left subtree.
    y.left = x.right
    if x.right
      x.right.not_nil!.parent = y
    end

    # Link y's parent to x.
    x.parent = y.parent
    unless y.parent
      @root = x;
    else
      if y == y.parent.not_nil!.left
        y.parent.not_nil!.left = x
      else
        y.parent.not_nil!.right = x
      end
    end

    x.right = y
    y.parent = x

    x
  end

  def remove_fixup(x : Node(K, V)?, x_parent : Node(K, V))
    while x_parent && x != @root && (!x || x.color == :black)
      if x == x_parent.left
        # Note: the text points out that w cannot be null. The reason is not obvious from
        # simply looking at the code; it comes about from the properties of the red-black
        # tree.
        w = x_parent.right.not_nil!
        if w.color == :red
          # Case 1
          w.color = :black
          x_parent.color = :red
          left_rotate(x_parent)
          w = x_parent.right.not_nil!
        end
        if (!w.left || w.left.not_nil!.color == :black) && (!w.right || w.right.not_nil!.color == :black)
          # Case 2
          w.color = :red
          x = x_parent
          x_parent = x.parent
        else
          if !w.right || w.right.not_nil!.color == :black
            # Case 3
            w.left.not_nil!.color = :black
            w.color = :red
            right_rotate(w)
            w = x_parent.right.not_nil!
          end
          # Case 4
          w.color = x_parent.color
          x_parent.color = :black
          if w.right
            w.right.not_nil!.color = :black
          end
          left_rotate(x_parent)
          x = @root.not_nil!
          x_parent = x.parent
        end
      else
        # Same as "then" clause with "right" and "left" exchanged.
        w = x_parent.left.not_nil!
        if w.color == :red
          # Case 1
          w.color = :black
          x_parent.color = :red
          right_rotate(x_parent)
          w = x_parent.left.not_nil!
        end
        if (!w.right || w.right.not_nil!.color == :black) && (!w.left || w.left.not_nil!.color == :black)
          # Case 2
          w.color = :red
          x = x_parent
          x_parent = x.parent
        else
          if !w.left || w.left.not_nil!.color == :black
            # Case 3
            w.right.not_nil!.color = :black
            w.color = :red
            left_rotate(w)
            w = x_parent.left.not_nil!
          end
          # Case 4
          w.color = x_parent.color
          x_parent.color = :black
          if w.left
            w.left.not_nil!.color = :black
          end
          right_rotate(x_parent)
          x = @root.not_nil!
          x_parent = x.parent
        end
      end
    end
    if x
      x.color = :black
    end
  end
end

class CallSign
  getter :value

  def initialize(value : Int32)
    @value = value
  end

  def compare_to(other)
    value == other.value ? 0 : ((value < other.value) ? -1 : 1)
  end

  def to_s
    "CS[#{@value}]"
  end
end

class Collision
  getter :aircraft_a, :aircraft_b, :position

  def initialize(aircraft_a : CallSign, aircraft_b : CallSign, position : Vector3D)
    @aircraft_a = aircraft_a
    @aircraft_b = aircraft_b
    @position  = position
  end
end

class CollisionDetector
  def initialize
    @state = RedBlackTree(CallSign, Vector3D).new
  end

  def handle_new_frame(frame)
    motions = Vector(Motion?).new
    seen    = RedBlackTree(CallSign, Bool).new

    frame.each { |aircraft|
      old_position = @state.put(aircraft.callsign, aircraft.position)
      new_position = aircraft.position
      seen.put(aircraft.callsign, true)

      if !old_position
        # Treat newly introduced aircraft as if they were stationary.
        old_position = new_position
      end

      motions.append(Motion.new(aircraft.callsign, old_position, new_position))
    }

    # Remove aircraft that are no longer present.
    to_remove = Vector(CallSign?).new
    @state.for_each { |e|
      unless seen.get(e.key)
        to_remove.append(e.key)
      end
    }

    to_remove.each { |e| @state.remove(e) }

    all_reduced = reduce_collision_set(motions)
    collisions = Vector(Collision?).new
    all_reduced.each { |reduced|
      (0...reduced.size).each { |i|
        motion1 = reduced.at(i).not_nil!
        ((i + 1)...reduced.size).each { |j|
          motion2 = reduced.at(j).not_nil!
          collision = motion1.find_intersection(motion2)
          if collision
            collisions.append(Collision.new(motion1.callsign, motion2.callsign, collision))
          end
    } } }

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

    (((xv == 0.0 && v_x <= x0 + r && x0 - r <= v_x + v_s) ||  # no motion in x
     (low_x <= 1.0 && 1.0 <= high_x) || (low_x <= 0.0 && 0.0 <= high_x) ||
      (0.0 <= low_x && high_x <= 1.0)) &&
    ((yv == 0.0 && v_y <= y0 + r && y0 - r <= v_y + v_s) ||  # no motion in y
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
      array = Vector(Motion?).new
      voxel_map.put(voxel, array)
    end
    array.not_nil!.append(motion)
  end

  def recurse(voxel_map, seen, next_voxel, motion)
    unless is_in_voxel(next_voxel, motion)
      return
    end

    if seen.put(next_voxel, true)
      return
    end

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
    voxel_map = RedBlackTree(Vector2D, Vector(Motion?)).new
    motions.each { |motion| draw_motion_on_voxel_map(voxel_map, motion) }

    result = Vector(Vector(Motion?)?).new
    voxel_map.for_each { |e|
      if e.value.size > 1
        result.append(e.value)
      end
    }
    result
  end

  def voxel_hash(position)
    x_div = (position.x / GOOD_VOXEL_SIZE).to_i
    y_div = (position.y / GOOD_VOXEL_SIZE).to_i

    x = GOOD_VOXEL_SIZE * x_div
    y = GOOD_VOXEL_SIZE * y_div

    if position.x < 0
      x -= GOOD_VOXEL_SIZE
    end
    if position.y < 0
      y -= GOOD_VOXEL_SIZE
    end

    Vector2D.new(x, y)
  end

  def draw_motion_on_voxel_map(voxel_map, motion)
    seen = RedBlackTree(Vector2D, Bool).new
    recurse(voxel_map, seen, voxel_hash(motion.pos_one), motion)
  end
end

class Motion
  getter :callsign, :pos_one, :pos_two

  def initialize(callsign : CallSign, pos_one : Vector3D, pos_two : Vector3D)
    @callsign = callsign
    @pos_one = pos_one
    @pos_two = pos_two
  end

  def to_s
    "Mot: #{@callsign} P1: #{@pos_one} P2: #{@pos_two}"
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

    # this test is not geometrical 3-d intersection test, it takes the fact that the aircraft move
    # into account ; so it is more like a 4d test
    # (it assumes that both of the aircraft have a constant speed over the tested interval)

    # we thus have two points, each of them moving on its line segment at constant speed ; we are looking
    # for times when the distance between these two points is smaller than r

    # vec1 is vector of aircraft 1
    # vec2 is vector of aircraft 2

    # a = (V2 - V1)^T * (V2 - V1)
    a = vec2.minus(vec1).squared_magnitude

    if a != 0.0
      # we are first looking for instances of time when the planes are exactly r from each other
      # at least one plane is moving ; if the planes are moving in parallel, they do not have constant speed

      # if the planes are moving in parallel, then
      #   if the faster starts behind the slower, we can have 2, 1, or 0 solutions
      #   if the faster plane starts in front of the slower, we can have 0 or 1 solutions

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
      if discr < 0.0
        return nil
      end

      v1 = (-b - Math.sqrt(discr)) / (2.0 * a)
      v2 = (-b + Math.sqrt(discr)) / (2.0 * a)

      if v1 <= v2 && ((v1  <= 1.0 && 1.0 <= v2) ||
          (v1  <= 0.0 && 0.0 <= v2) ||
          (0.0 <= v1  && v2  <= 1.0))
        # Pick a good "time" at which to report the collision.
        if v1 <= 0.0
            # The collision started before this frame. Report it at the start of the frame.
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

    # the planes have the same speeds and are moving in parallel (or they are not moving at all)
    # they  thus have the same distance all the time ; we calculate it from the initial point

    # dist = || i2 - i1 || = sqrt(  ( i2 - i1 )^T * ( i2 - i1 ) )
    dist = init2.minus(init1).magnitude
    if dist <= radius
      return init1.plus(init2).times(0.5)
    end

    nil
  end
end

class Aircraft
  getter :callsign, :position

  def initialize(callsign : CallSign, position : Vector3D)
    @callsign = callsign
    @position = position
  end

  def to_s
    "A#{@callsign} p: #{@position}"
  end
end

class Simulator
  def initialize(num_aircrafts)
    @aircraft = Vector(CallSign?).new
    (0...num_aircrafts).each { |i|
      @aircraft.append(CallSign.new(i))
    }
  end

  def simulate(time)
    frame = Vector(Aircraft?).new
    (0...@aircraft.size).step(2) { |i|
      frame.append(Aircraft.new(@aircraft.at(i).not_nil!,
        Vector3D.new(time, Math.cos(time) * 2 + i * 3, 10.0)))
      frame.append(Aircraft.new(@aircraft.at(i + 1).not_nil!,
        Vector3D.new(time, Math.sin(time) * 2 + i * 3, 10.0)))
    }
    frame
  end
end

class CD < Benchmark
  def benchmark(num_aircrafts)
    num_frames = 200
    simulator  = Simulator.new(num_aircrafts)
    detector   = CollisionDetector.new

    actual_collisions = 0

    (0...num_frames).each { |i|
      time = i / 10.0
      collisions = detector.handle_new_frame(simulator.simulate(time))
      actual_collisions += collisions.size
    }

    actual_collisions
  end

  def inner_benchmark_loop(inner_iterations)
    verify_result(benchmark(inner_iterations), inner_iterations)
  end

  def verify_result(actual_collisions, num_aircrafts)
    if num_aircrafts == 1000; return actual_collisions == 14484 end
    if num_aircrafts ==  500; return actual_collisions == 14484 end
    if num_aircrafts ==  250; return actual_collisions == 10830 end
    if num_aircrafts ==  200; return actual_collisions ==  8655 end
    if num_aircrafts ==  100; return actual_collisions ==  4305 end
    if num_aircrafts ==   10; return actual_collisions ==   390 end
    if num_aircrafts ==    2; return actual_collisions ==    42 end

    puts ("No verification result for " + num_aircrafts.to_s + " found")
    puts ("Result is: " + actual_collisions.to_s)
    false
  end
end
