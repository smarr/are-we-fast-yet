#pragma once

#include <any>
#include <cstdint>

class Benchmark {
 public:
  virtual ~Benchmark() = default;

  virtual std::any benchmark() = 0;
  virtual bool verify_result(std::any result) = 0;

  virtual bool inner_benchmark_loop(int32_t inner_iterations) {
    for (int32_t i = 0; i < inner_iterations; i += 1) {
      if (!verify_result(benchmark())) {
        return false;
      }
    }
    return true;
  }
};
