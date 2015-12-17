'use strict';

var benchmark = require('./benchmark.js'),
  som = require('./som.js');

function Bounce() {
  benchmark.Benchmark.call(this);
}
Bounce.prototype = Object.create(benchmark.Benchmark.prototype);

Bounce.prototype.benchmark = function () {
  var random  = new som.Random(),
    ballCount = 100,
    bounces   = 0,
    balls     = new Array(ballCount);

  for (var i = 0; i < ballCount; i++) {
    balls[i] = new Ball(random);
  }

  for (var i = 0; i < 50; i++) {
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

exports.newInstance = function () {
  return new Bounce();
};
