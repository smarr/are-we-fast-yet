#pragma once

#include <chrono>
#include <cstdio>

#include "bounce.h"
#include "mandelbrot.h"
#include "permute.h"
#include "queens.h"
#include "sieve.h"
#include "storage.h"
#include "towers.h"


static Benchmark* new_mandelbrot() {
  return new Mandelbrot();
}

static Benchmark* new_bounce() {
  return new Bounce();
}

static Benchmark* new_permute() {
  return new Permute();
}

static Benchmark* new_queens() {
  return new Queens();
}

static Benchmark* new_sieve() {
  return new Sieve();
}

static Benchmark* new_storage() {
  return new Storage();
}

static Benchmark* new_towers() {
  return new Towers();
}

using benchmark_suite = Benchmark* (*)();


class Run {
private:
  
  const std::string name;
  benchmark_suite suite;
  int32_t num_iterations;
  int32_t inner_iterations;
  int64_t total;

public:
  
  explicit Run(std::string& name) : name(name), suite(select_benchmark(name)) {
    num_iterations   = 1;
    inner_iterations = 1;
    total = 0;
  }
  
  void run_benchmark() {
    std::cout << "Starting " << name << " benchmark ...\n";
    Benchmark* benchmark = suite();
    do_runs(benchmark);
    delete benchmark;

    report_benchmark();
    std::cout << "\n";
  }
  
  void print_total() {
    std::cout << "Total Runtime: " << total << "us\n";
  }
  
  void set_num_iterations(int32_t num_iterations) {
    this->num_iterations = num_iterations;
  }
  
  void set_inner_iterations(int32_t inner_iterations) {
    this->inner_iterations = inner_iterations;
  }

private:

  benchmark_suite select_benchmark(std::string& name) {
    if (name == "Bounce") {
      return &new_bounce;
    }
    if (name == "Mandelbrot") {
      return &new_mandelbrot;
    }
    if (name == "Permute") {
      return &new_permute;
    }
    if (name == "Queens") {
      return &new_queens;
    }
    if (name == "Sieve") {
      return &new_sieve;
    }
    if (name == "Storage") {
      return &new_storage;
    }
    if (name == "Towers") {
      return &new_towers;
    }

    std::cerr << "Benchmark not recognized: " << name << "\n";
    exit(1);
  }
  
  void measure(Benchmark* const bench) {
    auto start_time = std::chrono::high_resolution_clock::now();
    if (!bench->inner_benchmark_loop(inner_iterations)) {
      std::cout << "Benchmark failed with incorrect result\n";
      exit(1); // TODO: should this be an exception?
    }
    auto end_time = std::chrono::high_resolution_clock::now();
    int64_t run_time = std::chrono::duration_cast<std::chrono::microseconds>(end_time - start_time).count();
    
    print_result(run_time);
    total += run_time;
  }
  
  void do_runs(Benchmark* const bench) {
    for (int32_t i = 0; i < num_iterations; i++) {
      measure(bench);
    }
  }
  
  void report_benchmark() {
    std::cout << name << ": iterations=" << num_iterations <<
                 " average: " << (total / num_iterations) << "us total: " << total << "us\n";
  }
  
  void print_result(int64_t run_time) {
    std::cout << name << ": iterations=1 runtime: " << run_time << "us\n";
  }
};
