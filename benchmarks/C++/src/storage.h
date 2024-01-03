#pragma once

#include <vector>

#include "benchmark.h"
#include "som/random.h"

class ArrayTree {
 public:
  ArrayTree* children{nullptr};

  ArrayTree() = default;
  ~ArrayTree() { delete[] children; }
};

class Storage : public Benchmark {
 private:
  int32_t count{0};

 public:
  Storage() = default;

  void* benchmark() override {
    Random random;

    count = 0;
    ArrayTree* result = build_tree_depth(7, random);
    delete[] result;

    return reinterpret_cast<void*>(static_cast<intptr_t>(count));
  }

  bool verify_result(void* result) override {
    return 5461 == static_cast<int32_t>(reinterpret_cast<intptr_t>(result));
  }

 private:
  ArrayTree* build_tree_depth(int32_t depth, Random& random) {
    count++;
    if (depth == 1) {
      return new ArrayTree[random.next() % 10 + 1];
    }

    auto* arr = new ArrayTree[4];
    for (size_t i = 0; i < 4; i++) {
      arr[i].children = build_tree_depth(depth - 1, random);
    }
    return arr;
  }
};
