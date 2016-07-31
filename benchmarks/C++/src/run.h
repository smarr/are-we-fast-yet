#pragma once

#include <chrono>
#include <stdio.h>

#include "mandelbrot.h"

class Run {
private:
  
  const std::string name;
  Benchmark* const benchmark;
  int32_t num_iterations;
  int32_t inner_iterations;
  int64_t total;

public:
  
  Run(std::string& name) : name(name), benchmark(select_benchmark(name)) {
    num_iterations   = 1;
    inner_iterations = 1;
    total = 0;
  }
  
  void run_benchmark() {
    std::cout << "Starting " << name << " benchmark ...\n";
    
    do_runs(benchmark);
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

  Benchmark* select_benchmark(std::string& name) {
    if (name == "Mandelbrot") {
      return new Mandelbrot();
    } else {
      std::cerr << "Benchmark not recognized: " << name << "\n";
      return nullptr;
    }
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
