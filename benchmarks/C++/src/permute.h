#pragma once

#include <any>

#include "benchmark.h"

class Permute : public Benchmark {
 private:
  int32_t count{0};
  int32_t* v{nullptr};

  void permute(int32_t n) {
    count += 1;
    if (n != 0) {
      const int32_t n1 = n - 1;
      permute(n1);
      for (int32_t i = n1; i >= 0; i -= 1) {
        swap(n1, i);
        permute(n1);
        swap(n1, i);
      }
    }
  }

  void swap(int32_t i, int32_t j) noexcept {
    const int32_t tmp = v[i];
    v[i] = v[j];
    v[j] = tmp;
  }

 public:
  std::any benchmark() override {
    count = 0;
    v = new int32_t[6];
    permute(6);
    delete[] v;
    return count;
  }

  bool verify_result(std::any result) override {
    return 8660 == std::any_cast<int32_t>(result);
  }
};
