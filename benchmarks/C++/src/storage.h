#pragma once

#include <vector>

#include "benchmark.h"
#include "som/random.h"

template <typename T>
class AllocMem {
public:
  static const size_t SIZE = 8 * 1024 * 1024;
  T* const mem;
  T* const end;
  mutable T* next;

  explicit AllocMem() : mem((T*) malloc(SIZE)),
  next(mem),
  end((T*)((uintptr_t) mem + (SIZE / sizeof(T)))) {}
  ~AllocMem() { free(mem); }
};


template <typename T>
class Allocator {
private:
  AllocMem<T>* const mem;

public:
  typedef T value_type;
  
  explicit Allocator(AllocMem<T>* mem) : mem(mem) {}
  Allocator(Allocator&&) = default;
  Allocator(const Allocator&) = default;
  
  T* allocate(std::size_t num_elements) const {
    if (num_elements + mem->next >= mem->end) {
      std::cout << "Failed to allocate memory";
      exit(0);
    }
    
    T* result = mem->next;
    mem->next += num_elements;
    return result;
  }
  
  void deallocate(T* p, std::size_t) const { }
  void reset() const { mem->next = mem->mem; }
};

typedef std::vector<void*, Allocator<void*>> vec;


class Storage : public Benchmark {
private:
  int32_t count;
  const Allocator<void*> vec_elem_alloc;
  const Allocator<vec>   vec_alloc;
  AllocMem<void*> alloc_elem;
  AllocMem<vec>   alloc_vec;

public:
  Storage() : vec_alloc(&alloc_vec), vec_elem_alloc(&alloc_elem), count(0) {}
  
  void* benchmark() override {
    Random random;
    
    count = 0;
    build_tree_depth(7, random);
    
    vec_elem_alloc.reset();
    vec_alloc.reset();
    
    return reinterpret_cast<void*>(intptr_t(count));
  }
  
  bool verify_result(void* result) override {
    return 5461 == int32_t(reinterpret_cast<intptr_t>(result));
  }

private:
  vec* build_tree_depth(int32_t depth, Random& random) {
    count++;
    if (depth == 1) {
      vec* mem = vec_alloc.allocate(1);
      return new(mem) vec(random.next() % 10 + 1, nullptr, vec_elem_alloc);
    }

    vec* mem = vec_alloc.allocate(1);
    vec* arr = new(mem) vec(4, nullptr, vec_elem_alloc);
    
    std::for_each(arr->begin(), arr->end(),
                  [this, depth, &random](void*& i) {
                    i = build_tree_depth(depth - 1, random); });
    return arr;
  }
};
