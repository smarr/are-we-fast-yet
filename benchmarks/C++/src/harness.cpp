#include <iostream>
#include <string>

#include "run.h"

static Run process_arguments(int32_t argc, const char* argv[]) {  // NOLINT
  std::string name(argv[1]);
  Run run(name);

  if (argc > 2) {
    run.set_num_iterations(std::stoi(argv[2]));
    if (argc > 3) {
      run.set_inner_iterations(std::stoi(argv[3]));
    }
  }

  return run;
}

static void print_usage() {
  std::cout << "./harness benchmark [num-iterations [inner-iter]]\n";
  std::cout << "\n";
  std::cout << "  benchmark      - benchmark class name\n";
  std::cout << "  num-iterations - number of times to execute benchmark, "
               "default: 1\n";
  std::cout << "  inner-iter     - number of times the benchmark is executed "
               "in an inner loop,\n";
  std::cout << "                   which is measured in total, default: 1\n";
}

int main(int argc, const char* argv[]) {
  if (argc < 2) {
    print_usage();
    return 1;
  }

  Run run = process_arguments(argc, argv);
  run.run_benchmark();
  run.print_total();

  return 0;
}
