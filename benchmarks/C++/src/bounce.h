#pragma once

#include <array>
#include <cstdlib>

#include "benchmark.h"
#include "som/random.h"

class Ball {
private:

  int32_t x;
  int32_t y;
  int32_t x_vel;
  int32_t y_vel;

public:
  Ball() {}
  
  Ball(Ball&&) = default;

  Ball(Random& random) {
    x = random.next() % 500;
    y = random.next() % 500;
    x_vel = (random.next() % 300) - 150;
    y_vel = (random.next() % 300) - 150;
  }
  
  Ball& operator=(const Ball&) = default;
  
  bool bounce() {
    int32_t x_limit = 500;
    int32_t y_limit = 500;
    bool bounced = false;
    
    x += x_vel;
    y += y_vel;
    if (x > x_limit) { x = x_limit; x_vel = 0 - abs(x_vel); bounced = true; }
    if (x < 0)       { x = 0;       x_vel = abs(x_vel);     bounced = true; }
    if (y > y_limit) { y = y_limit; y_vel = 0 - abs(y_vel); bounced = true; }
    if (y < 0)       { y = 0;       y_vel = abs(y_vel);     bounced = true; }
    return bounced;
  }
};

class Bounce : public Benchmark {
public:
  virtual void* benchmark() {
    Random random;
    
    const int32_t ball_count = 100;
    int32_t bounces = 0;
    Ball balls[ball_count];

    for (int32_t i = 0; i < ball_count; i++) {
      balls[i] = Ball(random);
    }
    
    for (int32_t j = 0; j < 50; j++) {
      for (int32_t i = 0; i < ball_count; i++) {
        if (balls[i].bounce()) {
          bounces++;
        }
      }
    }
    return (void*) (intptr_t) bounces;
  }
  
  virtual bool verify_result(void* result) {
    return 1331 == (int32_t) (intptr_t) result;
  }
};
