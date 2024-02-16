#pragma once

#include "benchmark.h"
#include "som/random.h"

#include <any>
#include <array>

class Ball {
 private:
  int32_t x;
  int32_t y;
  int32_t x_vel;
  int32_t y_vel;

 public:
  Ball() = default;

  Ball(Ball&&) = default;

  ~Ball() = default;

  explicit Ball(Random& random)
      : x(random.next() % 500),
        y(random.next() % 500),
        x_vel((random.next() % 300) - 150),
        y_vel((random.next() % 300) - 150) {}

  Ball& operator=(const Ball&) = default;

  bool bounce() {
    const int32_t x_limit = 500;
    const int32_t y_limit = 500;
    bool bounced = false;

    x += x_vel;
    y += y_vel;
    if (x > x_limit) {
      x = x_limit;
      x_vel = 0 - abs(x_vel);
      bounced = true;
    }
    if (x < 0) {
      x = 0;
      x_vel = abs(x_vel);
      bounced = true;
    }
    if (y > y_limit) {
      y = y_limit;
      y_vel = 0 - abs(y_vel);
      bounced = true;
    }
    if (y < 0) {
      y = 0;
      y_vel = abs(y_vel);
      bounced = true;
    }
    return bounced;
  }
};

class Bounce : public Benchmark {
 public:
  std::any benchmark() override {
    Random random;

    const int32_t ball_count = 100;
    int32_t bounces = 0;

    std::array<Ball, ball_count> balls = {};

    for (auto& ball : balls) {
      ball = Ball(random);
    }

    for (int32_t j = 0; j < 50; j += 1) {
      for (auto& ball : balls) {
        if (ball.bounce()) {
          bounces += 1;
        }
      }
    }
    return bounces;
  }

  bool verify_result(std::any result) override {
    return 1331 == std::any_cast<int32_t>(result);
  }
};
