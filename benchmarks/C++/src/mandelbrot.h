#pragma once

#include <any>
#include <iostream>

#include "benchmark.h"

class Mandelbrot : public Benchmark {
 public:
  std::any benchmark() override { return nullptr; }
  bool verify_result(std::any) override { return false; }

  bool inner_benchmark_loop(int32_t inner_iterations) override {
    return verify_result(mandelbrot(inner_iterations), inner_iterations);
  }

 private:
  bool verify_result(int32_t result, int32_t inner_iterations) {
    if (inner_iterations == 500) {
      return result == 191;
    }
    if (inner_iterations == 750) {
      return result == 50;
    }
    if (inner_iterations == 1) {
      return result == 128;
    }

    std::cout << "No verification result for " << inner_iterations
              << " found\n";
    std::cout << "Result is: " << result << "\n";
    return false;
  }

  int32_t mandelbrot(int32_t size) {
    int32_t sum = 0;
    int32_t byte_acc = 0;
    int32_t bit_num = 0;

    int32_t y = 0;

    while (y < size) {
      const double ci = (2.0 * y / size) - 1.0;
      int32_t x = 0;

      while (x < size) {
        double zrzr = 0.0;
        double zi = 0.0;
        double zizi = 0.0;
        const double cr = (2.0 * x / size) - 1.5;

        int32_t z = 0;
        bool notDone = true;
        int32_t escape = 0;
        while (notDone && z < 50) {
          const double zr = zrzr - zizi + cr;
          zi = 2.0 * zr * zi + ci;

          // preserve recalculation
          zrzr = zr * zr;
          zizi = zi * zi;

          if (zrzr + zizi > 4.0) {
            notDone = false;
            escape = 1;
          }
          z += 1;
        }

        byte_acc = (byte_acc << 1) + escape;
        bit_num += 1;

        // Code is very similar for these cases, but using separate blocks
        // ensures we skip the shifting when it's unnecessary, which is most
        // cases.
        if (bit_num == 8) {
          sum ^= byte_acc;
          byte_acc = 0;
          bit_num = 0;
        } else if (x == size - 1) {
          byte_acc <<= (8 - bit_num);
          sum ^= byte_acc;
          byte_acc = 0;
          bit_num = 0;
        }
        x += 1;
      }
      y += 1;
    }
    return sum;
  }
};
