#pragma once

#include <chrono>
#include <cstdio>

#include "bounce.h"
#include "cd.h"
#include "deltablue.h"
#include "havlak.h"
#include "json.h"
#include "list.h"
#include "mandelbrot.h"
#include "nbody.h"
#include "permute.h"
#include "queens.h"
#include "richards.h"
#include "sieve.h"
#include "storage.h"
#include "towers.h"

using benchmark_suite_ctr = Benchmark* (*)();

class Run {
 private:
  const std::string name;
  benchmark_suite_ctr suite;
  int32_t num_iterations{1};
  int32_t inner_iterations{1};
  int64_t total{0};

 public:
  explicit Run(std::string& name) : name(name), suite(select_benchmark(name)) {}

  void run_benchmark() {
    std::cout << "Starting " << name << " benchmark ...\n";
    Benchmark* benchmark = suite();
    do_runs(benchmark);
    delete benchmark;

    report_benchmark();
    std::cout << "\n";
  }

  void print_total() const {
    std::cout << "Total Runtime: " << total << "us\n";
  }

  void set_num_iterations(int32_t num_iterations) {
    this->num_iterations = num_iterations;
  }

  void set_inner_iterations(int32_t inner_iterations) {
    this->inner_iterations = inner_iterations;
  }

 private:
  benchmark_suite_ctr select_benchmark(std::string& name) {
    if (name == "Bounce") {
      return []() -> Benchmark* { return new Bounce(); };
    }
    if (name == "CD") {
      return []() -> Benchmark* { return new CD(); };
    }
    if (name == "Havlak") {
      return []() -> Benchmark* { return new Havlak(); };
    }
    if (name == "Json") {
      return []() -> Benchmark* { return new Json(); };
    }
    if (name == "List") {
      return []() -> Benchmark* { return new List(); };
    }
    if (name == "Mandelbrot") {
      return []() -> Benchmark* { return new Mandelbrot(); };
    }
    if (name == "NBody") {
      return []() -> Benchmark* { return new NBody(); };
    }
    if (name == "Permute") {
      return []() -> Benchmark* { return new Permute(); };
    }
    if (name == "Queens") {
      return []() -> Benchmark* { return new Queens(); };
    }
    if (name == "Richards") {
      return []() -> Benchmark* { return new Richards(); };
    }
    if (name == "Sieve") {
      return []() -> Benchmark* { return new Sieve(); };
    }
    if (name == "Storage") {
      return []() -> Benchmark* { return new Storage(); };
    }
    if (name == "Towers") {
      return []() -> Benchmark* { return new Towers(); };
    }
    if (name == "DeltaBlue") {
      return []() -> Benchmark* { return new DeltaBlue(); };
    }

    std::cerr << "Benchmark not recognized: " << name << "\n";
    exit(1);
  }

  void measure(Benchmark* const bench) {
    auto start_time = std::chrono::high_resolution_clock::now();
    if (!bench->inner_benchmark_loop(inner_iterations)) {
      std::cout << "Benchmark failed with incorrect result\n";
      exit(1);
    }
    auto end_time = std::chrono::high_resolution_clock::now();
    const int64_t run_time =
        std::chrono::duration_cast<std::chrono::microseconds>(end_time -
                                                              start_time)
            .count();

    print_result(run_time);
    total += run_time;
  }

  void do_runs(Benchmark* const bench) {
    for (int32_t i = 0; i < num_iterations; i += 1) {
      measure(bench);
    }
  }

  void report_benchmark() {
    std::cout << name << ": iterations=" << num_iterations
              << " average: " << (total / num_iterations)
              << "us total: " << total << "us\n";
  }

  void print_result(int64_t run_time) {
    std::cout << name << ": iterations=1 runtime: " << run_time << "us\n";
  }
};
