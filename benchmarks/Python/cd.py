# Copyright (c) 2001-2021 Stefan Marr
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
from enum import Enum
from math import sqrt, cos, sin

from benchmark import Benchmark
from som.vector import Vector

MIN_X = 0.0
MIN_Y = 0.0
MAX_X = 1000.0
MAX_Y = 1000.0
MIN_Z = 0.0
MAX_Z = 10.0
PROXIMITY_RADIUS = 1.0
GOOD_VOXEL_SIZE = PROXIMITY_RADIUS * 2.0


def _compare_numbers(a, b):
    if a == b:
        return 0

    if a < b:
        return -1

    if a > b:
        return 1

    # We say that NaN is smaller than non-NaN.
    if a == a:  # pylint: disable=comparison-with-itself
        return 1

    return -1


class _Vector2D:
    def __init__(self, x, y):
        self.x = x
        self.y = y

    def plus(self, other):
        return _Vector2D(self.x + other.x, self.y + other.y)

    def minus(self, other):
        return _Vector2D(self.x - other.x, self.y - other.y)

    def compare_to(self, other):
        result = _compare_numbers(self.x, other.x)
        if result != 0:
            return result

        return _compare_numbers(self.y, other.y)


class _Vector3D:
    def __init__(self, x, y, z):
        self.x = x
        self.y = y
        self.z = z

    def plus(self, other):
        return _Vector3D(self.x + other.x, self.y + other.y, self.z + other.z)

    def minus(self, other):
        return _Vector3D(self.x - other.x, self.y - other.y, self.z - other.z)

    def dot(self, other):
        return self.x * other.x + self.y * other.y + self.z * other.z

    def squared_magnitude(self):
        return self.dot(self)

    def magnitude(self):
        return sqrt(self.squared_magnitude())

    def times(self, amount):
        return _Vector3D(self.x * amount, self.y * amount, self.z * amount)


_horizontal = _Vector2D(GOOD_VOXEL_SIZE, 0.0)
_vertical = _Vector2D(0.0, GOOD_VOXEL_SIZE)


class _Color(Enum):
    RED = 1
    BLACK = 2


def _tree_minimum(x):
    current = x
    while current.left is not None:
        current = current.left

    return current


class _Node:
    def __init__(self, key, value):
        self.key = key
        self.value = value
        self.left = None
        self.right = None
        self.parent = None
        self.color = _Color.RED

    def successor(self):
        x = self
        if x.right is not None:
            return _tree_minimum(x.right)

        y = x.parent
        while y is not None and x is y.right:
            x = y
            y = y.parent

        return y


class _Entry:
    def __init__(self, key, value):
        self.key = key
        self.value = value


class _InsertResult:
    def __init__(self, is_new_entry, new_node, old_value):
        self.is_new_entry = is_new_entry
        self.new_node = new_node
        self.old_value = old_value


class _RedBlackTree:
    def __init__(self):
        self._root = None

    def put(self, key, value):
        insertion_result = self._tree_insert(key, value)
        if not insertion_result.is_new_entry:
            return insertion_result.old_value

        x = insertion_result.new_node

        while x is not self._root and x.parent.color is _Color.RED:
            if x.parent is x.parent.parent.left:
                y = x.parent.parent.right
                if y is not None and y.color is _Color.RED:
                    # Case 1
                    x.parent.color = _Color.BLACK
                    y.color = _Color.BLACK
                    x.parent.parent.color = _Color.RED
                    x = x.parent.parent
                else:
                    if x is x.parent.right:
                        # Case 2
                        x = x.parent
                        self._left_rotate(x)

                    # Case 3
                    x.parent.color = _Color.BLACK
                    x.parent.parent.color = _Color.RED
                    self._right_rotate(x.parent.parent)

            else:
                # Same as "then" clause with "right" and "left" exchanged.
                y = x.parent.parent.left
                if y is not None and y.color is _Color.RED:
                    # Case 1
                    x.parent.color = _Color.BLACK
                    y.color = _Color.BLACK
                    x.parent.parent.color = _Color.RED
                    x = x.parent.parent
                else:
                    if x is x.parent.left:
                        # Case 2
                        x = x.parent
                        self._right_rotate(x)

                    # Case 3
                    x.parent.color = _Color.BLACK
                    x.parent.parent.color = _Color.RED
                    self._left_rotate(x.parent.parent)

        self._root.color = _Color.BLACK
        return None

    def remove(self, key):
        z = self._find_node(key)
        if z is None:
            return None

        # y is the node to be unlinked from the tree.
        if z.left is None or z.right is None:
            y = z
        else:
            y = z.successor()

        # y is guaranteed to be non-null at this point.
        if y.left is not None:
            x = y.left
        else:
            x = y.right

        # x is the child of y which might potentially replace y in the tree. X might be null at
        # this point.
        if x is not None:
            x.parent = y.parent
            x_parent = x.parent
        else:
            x_parent = y.parent

        if y.parent is None:
            self._root = x
        else:
            if y is y.parent.left:
                y.parent.left = x
            else:
                y.parent.right = x

        if y is not z:
            if y.color is _Color.BLACK:
                self._remove_fixup(x, x_parent)

            y.parent = z.parent
            y.color = z.color
            y.left = z.left
            y.right = z.right

            if z.left is not None:
                z.left.parent = y
            if z.right is not None:
                z.right.parent = y
            if z.parent is not None:
                if z.parent.left == z:
                    z.parent.left = y
                else:
                    z.parent.right = y
            else:
                self._root = y
        elif y.color is _Color.BLACK:
            self._remove_fixup(x, x_parent)

        return z.value

    def get(self, key):
        node = self._find_node(key)
        if node is None:
            return None

        return node.value

    def for_each(self, fn):
        if self._root is None:
            return

        current = _tree_minimum(self._root)
        while current is not None:
            fn(_Entry(current.key, current.value))
            current = current.successor()

    def _find_node(self, key):
        current = self._root
        while current is not None:
            comparison_result = key.compare_to(current.key)
            if comparison_result == 0:
                return current
            if comparison_result < 0:
                current = current.left
            else:
                current = current.right
        return None

    def _tree_insert(self, key, value):
        y = None
        x = self._root

        while x is not None:
            y = x
            comparison_result = key.compare_to(x.key)
            if comparison_result < 0:
                x = x.left
            elif comparison_result > 0:
                x = x.right
            else:
                old_value = x.value
                x.value = value
                return _InsertResult(False, None, old_value)

        z = _Node(key, value)
        z.parent = y
        if y is None:
            self._root = z
        else:
            if key.compare_to(y.key) < 0:
                y.left = z
            else:
                y.right = z
        return _InsertResult(True, z, None)

    def _left_rotate(self, x):
        y = x.right

        # Turn y's left subtree into x's right subtree.
        x.right = y.left
        if y.left is not None:
            y.left.parent = x

        # Link x's parent to y.
        y.parent = x.parent
        if x.parent is None:
            self._root = y
        else:
            if x is x.parent.left:
                x.parent.left = y
            else:
                x.parent.right = y

        # Put x on y's left.
        y.left = x
        x.parent = y

        return y

    def _right_rotate(self, y):
        x = y.left

        # Turn x's right subtree into y's left subtree.
        y.left = x.right
        if x.right is not None:
            x.right.parent = y

        # Link y's parent to x;
        x.parent = y.parent
        if y.parent is None:
            self._root = x
        else:
            if y is y.parent.left:
                y.parent.left = x
            else:
                y.parent.right = x

        x.right = y
        y.parent = x

        return x

    def _remove_fixup(self, x, x_parent):
        while x is not self._root and (x is None or x.color is _Color.BLACK):
            if x is x_parent.left:
                # Note: the text points out that w cannot be null. The reason is not obvious from
                # simply looking at the code; it comes about from the properties of the red-black
                # tree.
                w = x_parent.right
                if w.color is _Color.RED:
                    # Case 1
                    w.color = _Color.BLACK
                    x_parent.color = _Color.RED
                    self._left_rotate(x_parent)
                    w = x_parent.right

                if (w.left is None or w.left.color is _Color.BLACK) and (
                    w.right is None or w.right.color is _Color.BLACK
                ):
                    # Case 2
                    w.color = _Color.RED
                    x = x_parent
                    x_parent = x.parent
                else:
                    if w.right is None or w.right.color is _Color.BLACK:
                        # Case 3
                        w.left.color = _Color.BLACK
                        w.color = _Color.RED
                        self._right_rotate(w)
                        w = x_parent.right

                    # Case 4
                    w.color = x_parent.color
                    x_parent.color = _Color.BLACK
                    if w.right is not None:
                        w.right.color = _Color.BLACK

                    self._left_rotate(x_parent)
                    x = self._root
                    x_parent = x.parent
            else:
                # Same as "then" clause with "right" and "left" exchanged.
                w = x_parent.left
                if w.color is _Color.RED:
                    # Case 1
                    w.color = _Color.BLACK
                    x_parent.color = _Color.RED
                    self._right_rotate(x_parent)
                    w = x_parent.left

                if (w.right is None or w.right.color is _Color.BLACK) and (
                    w.left is None or w.left.color is _Color.BLACK
                ):
                    # Case 2
                    w.color = _Color.RED
                    x = x_parent
                    x_parent = x.parent
                else:
                    if w.left is None or w.left.color is _Color.BLACK:
                        # Case 3
                        w.right.color = _Color.BLACK
                        w.color = _Color.RED
                        self._left_rotate(w)
                        w = x_parent.left

                    # Case 4
                    w.color = x_parent.color
                    x_parent.color = _Color.BLACK
                    if w.left is not None:
                        w.left.color = _Color.BLACK

                    self._right_rotate(x_parent)
                    x = self._root
                    x_parent = x.parent

        if x is not None:
            x.color = _Color.BLACK


class _CallSign:
    def __init__(self, value):
        self._value = value

    def compare_to(self, other):
        if self._value == other._value:  # pylint: disable=protected-access
            return 0
        if self._value < other._value:  # pylint: disable=protected-access
            return -1
        return 1


class _Collision:
    def __init__(self, aircraft_a, aircraft_b, position):
        self.aircraft_a = aircraft_a
        self.aircraft_b = aircraft_b
        self.position = position


class _CollisionDetector:
    def __init__(self):
        self._state = _RedBlackTree()

    def handle_new_frame(self, frame):
        motions = Vector()
        seen = _RedBlackTree()

        def each(aircraft):
            old_position = self._state.put(aircraft.call_sign, aircraft.position)
            new_position = aircraft.position
            seen.put(aircraft.call_sign, True)

            if old_position is None:
                # Treat newly introduced aircraft as if they were stationary.
                old_position = new_position

            motions.append(_Motion(aircraft.call_sign, old_position, new_position))

        frame.for_each(each)

        # Remove aircraft that are no longer present.
        to_remove = Vector()

        def for_removal(e):
            if not seen.get(e.key):
                to_remove.append(e.key)

        self._state.for_each(for_removal)

        to_remove.for_each(self._state.remove)

        all_reduced = _reduce_collision_set(motions)
        collisions = Vector()

        def find_collisions(reduced):
            for i in range(reduced.size()):
                motion1 = reduced.at(i)
                for j in range(i + 1, reduced.size()):
                    motion2 = reduced.at(j)
                    collision = motion1.find_intersection(motion2)
                    if collision is not None:
                        collisions.append(
                            _Collision(motion1.call_sign, motion2.call_sign, collision)
                        )

        all_reduced.for_each(find_collisions)

        return collisions


_inf_positive = float("inf")
_inf_negative = float("-inf")


def _is_in_voxel(voxel, motion):
    if voxel.x > MAX_X or voxel.x < MIN_X or voxel.y > MAX_Y or voxel.y < MIN_Y:
        return False

    init = motion.pos_one
    fin = motion.pos_two

    v_s = GOOD_VOXEL_SIZE
    r = PROXIMITY_RADIUS / 2.0

    v_x = voxel.x
    x0 = init.x
    xv = fin.x - init.x

    v_y = voxel.y
    y0 = init.y
    yv = fin.y - init.y

    if xv == 0.0:
        low_x = _inf_negative if (v_x - r - x0) < 0.0 else _inf_positive
        high_x = _inf_negative if (v_x + v_s + r - x0) < 0.0 else _inf_positive
    else:
        low_x = (v_x - r - x0) / xv
        high_x = (v_x + v_s + r - x0) / xv

    if xv < 0.0:
        low_x, high_x = high_x, low_x

    if yv == 0.0:
        low_y = _inf_negative if (v_y - r - y0) < 0.0 else _inf_positive
        high_y = _inf_negative if (v_y + v_s + r - y0) < 0.0 else _inf_positive
    else:
        low_y = (v_y - r - y0) / yv
        high_y = (v_y + v_s + r - y0) / yv

    if yv < 0.0:
        low_y, high_y = high_y, low_y

    return (
        (
            (xv == 0.0 and v_x <= x0 + r and x0 - r <= v_x + v_s)
            or (low_x <= 1.0 and 1.0 <= high_x)  # no motion in x
            or (low_x <= 0.0 and 0.0 <= high_x)
            or (0.0 <= low_x and high_x <= 1.0)
        )
        and (
            (yv == 0.0 and v_y <= y0 + r and y0 - r <= v_y + v_s)
            or (  # no motion in y
                (low_y <= 1.0 and 1.0 <= high_y)
                or (low_y <= 0.0 and 0.0 <= high_y)
                or (0.0 <= low_y and high_y <= 1.0)
            )
        )
        and (
            xv == 0.0
            or yv == 0.0
            or (low_y <= high_x and high_x <= high_y)  # no motion in x or y or both
            or (low_y <= low_x and low_x <= high_y)
            or (low_x <= low_y and high_y <= high_x)
        )
    )


def _put_into_map(voxel_map, voxel, motion):
    array = voxel_map.get(voxel)
    if array is None:
        array = Vector()
        voxel_map.put(voxel, array)
    array.append(motion)


def _recurse(voxel_map, seen, next_voxel, motion):
    if not _is_in_voxel(next_voxel, motion):
        return

    if seen.put(next_voxel, True):
        return

    _put_into_map(voxel_map, next_voxel, motion)

    _recurse(voxel_map, seen, next_voxel.minus(_horizontal), motion)
    _recurse(voxel_map, seen, next_voxel.plus(_horizontal), motion)
    _recurse(voxel_map, seen, next_voxel.minus(_vertical), motion)
    _recurse(voxel_map, seen, next_voxel.plus(_vertical), motion)
    _recurse(voxel_map, seen, next_voxel.minus(_horizontal).minus(_vertical), motion)
    _recurse(voxel_map, seen, next_voxel.minus(_horizontal).plus(_vertical), motion)
    _recurse(voxel_map, seen, next_voxel.plus(_horizontal).minus(_vertical), motion)
    _recurse(voxel_map, seen, next_voxel.plus(_horizontal).plus(_vertical), motion)


def _reduce_collision_set(motions):
    voxel_map = _RedBlackTree()
    motions.for_each(lambda motion: _draw_motion_on_voxel_map(voxel_map, motion))

    result = Vector()

    def each(e):
        if e.value.size() > 1:
            result.append(e.value)

    voxel_map.for_each(each)
    return result


def _voxel_hash(position):
    x_div = position.x // GOOD_VOXEL_SIZE
    y_div = position.y // GOOD_VOXEL_SIZE

    x = GOOD_VOXEL_SIZE * x_div
    y = GOOD_VOXEL_SIZE * y_div

    if position.x < 0.0:
        x -= GOOD_VOXEL_SIZE

    if position.y < 0.0:
        y -= GOOD_VOXEL_SIZE

    return _Vector2D(x, y)


def _draw_motion_on_voxel_map(voxel_map, motion):
    seen = _RedBlackTree()
    _recurse(voxel_map, seen, _voxel_hash(motion.pos_one), motion)


class _Motion:
    def __init__(self, call_sign, pos_one, pos_two):
        self.call_sign = call_sign
        self.pos_one = pos_one
        self.pos_two = pos_two

    def delta(self):
        return self.pos_two.minus(self.pos_one)

    def find_intersection(self, other):
        init1 = self.pos_one
        init2 = other.pos_one
        vec1 = self.delta()
        vec2 = other.delta()
        radius = PROXIMITY_RADIUS

        # this test is not geometrical 3-d intersection test,
        # it takes the fact that the aircraft move
        # into account ; so it is more like a 4d test
        # (it assumes that both of the aircraft have a constant speed over the tested interval)

        # we thus have two points, each of them moving on its line segment at constant speed ;
        # we are looking for times when the distance between these two points is smaller than r

        # vec1 is vector of aircraft 1
        # vec2 is vector of aircraft 2

        # a = (V2 - V1)^T * (V2 - V1)
        a = vec2.minus(vec1).squared_magnitude()

        if a != 0.0:
            # we are first looking for instances of time when the planes are
            # exactly r from each other at least one plane is moving ;
            # if the planes are moving in parallel, they do not have constant speed

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
            c = -radius * radius + init2.minus(init1).squared_magnitude()

            discr = b * b - 4.0 * a * c
            if discr < 0.0:
                return None

            v1 = (-b - sqrt(discr)) / (2.0 * a)
            v2 = (-b + sqrt(discr)) / (2.0 * a)

            if v1 <= v2 and (
                (v1 <= 1.0 and 1.0 <= v2)
                or (v1 <= 0.0 and 0.0 <= v2)
                or (0.0 <= v1 and v2 <= 1.0)
            ):
                # Pick a good "time" at which to report the collision.
                if v1 <= 0.0:
                    # The collision started before this frame. Report it at the start of the frame.
                    v = 0.0
                else:
                    # The collision started during this frame. Report it at that moment.
                    v = v1

                result1 = init1.plus(vec1.times(v))
                result2 = init2.plus(vec2.times(v))

                result = result1.plus(result2).times(0.5)
                if (
                    result.x >= MIN_X
                    and result.x <= MAX_X
                    and result.y >= MIN_Y
                    and result.y <= MAX_Y
                    and result.z >= MIN_Z
                    and result.z <= MAX_Z
                ):
                    return result

            return None

        # the planes have the same speeds and are moving in parallel (or they are not moving at all)
        # they  thus have the same distance all the time ; we calculate it from the initial point

        # dist = || i2 - i1 || = sqrt(  ( i2 - i1 )^T * ( i2 - i1 ) )
        dist = init2.minus(init1).magnitude()
        if dist <= radius:
            return init1.plus(init2).times(0.5)

        return None


class _Aircraft:
    def __init__(self, call_sign, position):
        self.call_sign = call_sign
        self.position = position


class _Simulator:
    def __init__(self, num_aircraft):
        self._aircraft = Vector()
        for i in range(num_aircraft):
            self._aircraft.append(_CallSign(i))

    def simulate(self, time):
        frame = Vector()
        for i in range(0, self._aircraft.size(), 2):
            frame.append(
                _Aircraft(
                    self._aircraft.at(i),
                    _Vector3D(time, cos(time) * 2.0 + i * 3.0, 10.0),
                )
            )

            frame.append(
                _Aircraft(
                    self._aircraft.at(i + 1),
                    _Vector3D(time, sin(time) * 2.0 + i * 3.0, 10.0),
                )
            )
        return frame


class CD(Benchmark):
    @staticmethod
    def _benchmark(num_aircraft):
        num_frames = 200

        simulator = _Simulator(num_aircraft)
        detector = _CollisionDetector()

        actual_collisions = 0

        for i in range(num_frames):
            time = i / 10.0
            collisions = detector.handle_new_frame(simulator.simulate(time))
            actual_collisions += collisions.size()

        return actual_collisions

    def inner_benchmark_loop(self, inner_iterations):
        return self._verify_result(self._benchmark(inner_iterations), inner_iterations)

    @staticmethod
    def _verify_result(actual_collisions, num_aircraft):
        if num_aircraft == 1000:
            return actual_collisions == 14484
        if num_aircraft == 500:
            return actual_collisions == 14484
        if num_aircraft == 250:
            return actual_collisions == 10830
        if num_aircraft == 200:
            return actual_collisions == 8655
        if num_aircraft == 100:
            return actual_collisions == 4305
        if num_aircraft == 10:
            return actual_collisions == 390
        if num_aircraft == 2:
            return actual_collisions == 42

        print("No verification result for " + str(num_aircraft) + " found")
        print("Result is: " + str(actual_collisions))
        return False

    def benchmark(self):
        raise Exception("Should never be reached")

    def verify_result(self, result):
        raise Exception("Should never be reached")
