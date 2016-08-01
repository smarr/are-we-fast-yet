#pragma once

#include <vector>

#include "benchmark.h"
#include "som/random.h"


class Storage : public Benchmark {
private:
  int32_t count;

public:
  virtual void* benchmark() {
    Random random;
    
    count = 0;
    std::vector<void*>* tree = build_tree_depth(7, random);
    free_tree(tree);
    return (void*) (intptr_t) count;
  }
  
  virtual bool verify_result(void* result) {
    return 5461 == (int32_t) (intptr_t) result;
  }

private:
  std::vector<void*>* build_tree_depth(int32_t depth, Random& random) {
    count++;
    if (depth == 1) {
      return new std::vector<void*>(random.next() % 10 + 1);
    } else {
      auto arr = new std::vector<void*>(4);
      
      std::for_each(arr->begin(), arr->end(),
                    [this, depth, &random](void*& i) {
                      i = build_tree_depth(depth - 1, random); });
      return arr;
    }
  }
  
  void free_tree(std::vector<void*>* tree) {
    std::for_each(tree->begin(), tree->end(),
                  [this](void*& i) {
                    if (i != nullptr) {
                      std::vector<void*>* t = (std::vector<void*>*) i;
                      free_tree(t);
                    } });
    delete tree;
  }
};
