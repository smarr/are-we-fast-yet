#pragma once

class Benchmark {
public:
  virtual ~Benchmark() {}

  virtual void* benchmark() = 0;
  virtual bool  verify_result(void* result) = 0;
  
  virtual bool inner_benchmark_loop(int32_t inner_iterations) {
    for (int32_t i = 0; i < inner_iterations; i++) {
      if (!verify_result(benchmark())) {
        return false;
      }
    }
    return true;
  }
};
