#pragma once

#include "benchmark.h"

class Sieve : public Benchmark {
 public:
  void* benchmark() override {
    const int32_t num_flags = 5000;
    std::array<bool, num_flags> flags{};

    std::fill_n(flags.begin(), num_flags, true);

    return reinterpret_cast<void*>(
        static_cast<intptr_t>(sieve<num_flags>(flags, 5000)));
  }

  bool verify_result(void* result) override {
    return 669 == static_cast<int32_t>(reinterpret_cast<intptr_t>(result));
  }

 private:
  template <int32_t Size>
  int32_t sieve(std::array<bool, Size> flags, int32_t size) {
    int32_t prime_count = 0;

    for (int32_t i = 2; i <= size; i++) {
      if (flags[i - 1]) {
        prime_count++;
        int k = i + i;
        while (k <= size) {
          flags[k - 1] = false;
          k += i;
        }
      }
    }
    return prime_count;
  }
};
