// This code is derived from the SOM benchmarks, see AUTHORS.md file.
//
// Copyright (c) 2015-2016 Stefan Marr <git@stefan-marr.de>
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the 'Software'), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
'use strict';

function Bounce() {
  Benchmark.call(this);
}
Bounce.prototype = Object.create(Benchmark.prototype);

Bounce.prototype.benchmark = function () {
  var random  = new som.Random(),
    ballCount = 100,
    bounces   = 0,
    balls     = new Array(ballCount),
    i = 0;

  for (i = 0; i < ballCount; i++) {
    balls[i] = new Ball(random);
  }

  for (i = 0; i < 50; i++) {
    balls.forEach(function (ball) {
      if (ball.bounce()) {
        bounces += 1;
      }
    });
  }
  return bounces;
};

Bounce.prototype.verifyResult = function (result) {
  return result == 1331;
};

function Ball(random) {
  this.x = random.next() % 500;
  this.y = random.next() % 500;
  this.xVel = (random.next() % 300) - 150;
  this.yVel = (random.next() % 300) - 150;
}

Ball.prototype.bounce = function () {
  var xLimit = 500,
    yLimit   = 500,
    bounced  = false;

  this.x += this.xVel;
  this.y += this.yVel;

  if (this.x > xLimit) {
    this.x = xLimit; this.xVel = 0 - Math.abs(this.xVel); bounced = true;
  }

  if (this.x < 0) {
    this.x = 0;      this.xVel = Math.abs(this.xVel);     bounced = true;
  }

  if (this.y > yLimit) {
    this.y = yLimit; this.yVel = 0 - Math.abs(this.yVel); bounced = true;
  }

  if (this.y < 0) {
    this.y = 0;      this.yVel = Math.abs(this.yVel);     bounced = true;
  }

  return bounced;
};

var suite = {
  newInstance: function () {
    return new Bounce();
  },
  name: "Bounce"
}

var run = new Run(Bounce, "Bounce", 100, 1500);
run.runBenchmark();
run.printTotal();
