# This code is based on the SOM class library.
#
# Copyright (c) 2001-2021 see AUTHORS.md file
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
from benchmark import Benchmark
from som.random import Random


class Ball:
    def __init__(self, random):
        self._x = random.next() % 500
        self._y = random.next() % 500
        self._x_vel = (random.next() % 300) - 150
        self._y_vel = (random.next() % 300) - 150

    def bounce(self):
        x_limit = 500
        y_limit = 500
        bounced = False

        self._x += self._x_vel
        self._y += self._y_vel

        if self._x > x_limit:
            self._x = x_limit
            self._x_vel = -abs(self._x_vel)
            bounced = True

        if self._x < 0:
            self._x = 0
            self._x_vel = abs(self._x_vel)
            bounced = True

        if self._y > y_limit:
            self._y = y_limit
            self._y_vel = -abs(self._y_vel)
            bounced = True

        if self._y < 0:
            self._y = 0
            self._y_vel = abs(self._y_vel)
            bounced = True

        return bounced


class Bounce(Benchmark):
    def benchmark(self):
        random = Random()

        ball_count = 100
        bounces = 0
        balls = [None] * ball_count

        for i in range(ball_count):
            balls[i] = Ball(random)

        for i in range(50):
            for ball in balls:
                if ball.bounce():
                    bounces += 1

        return bounces

    def verify_result(self, result):
        return result == 1331
