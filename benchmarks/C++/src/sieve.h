#pragma once

#include "benchmark.h"


class Sieve : public Benchmark {
public:
  
  void* benchmark() override {
    const int32_t num_flags = 5000;
    bool flags[num_flags];
    
    for (int32_t i = 0; i < num_flags; i++) {
      flags[i] = true;
    }
    
    return (void*) (intptr_t) sieve(flags, 5000);
  }

    return 669 == (int32_t) (intptr_t) result;
  bool verify_result(void* result) override {
  }

private:

  int32_t sieve(bool flags[], int32_t size) {
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
