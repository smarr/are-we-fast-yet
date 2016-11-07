# try to follow style guide: http://r-pkgs.had.co.nz/style.html
source("benchmark.R")

Run <- function() {
  structure(list(
      name             ="",
      benchmark_suite  = NULL,
      num_iterations   = 1,
      inner_iterations = 1,
      total            = 0), 
    class = "Run")
}

run_benchmark <- function(r) {
  writeLines(paste0("Starting ", r$name, " benchmark ..."))
  r <- do_runs(r, r$benchmark_suite())
  report_benchmark(r)
  writeLines("")
}

measure <- function(r, bench) {
  # TODO: ask for platform independent way of measuring milliseconds/microseconds
  start_time <- proc.time()[[3]]
  if (!inner_benchmark_loop(bench, r$inner_iterations)) {
    stop(simpleError("Benchmark failed with incorrect result"))
  }
  end_time <- proc.time()[[3]]
  run_time <- (end_time - start_time) * 1000 * 1000 # we need to report microseconds
  print_result(r, run_time)
  r$total <- r$total + run_time
  r
}

do_runs <- function(r, bench) {
  for (i in 1:r$num_iterations) {
    r <- measure(r, bench)
  }
  r
}

report_benchmark <- function (r) {
  writeLines(paste0(r$name, ": iterations=", r$num_iterations,
                    " average: ", r$total / r$num_iterations, "us total: ",
                    r$total, "us\n"))
}

print_result <- function (r, run_time) {
  writeLines(paste0(r$name, ": iterations=1 runtime: ", runTime, "us"))
}

print_total <- function (r) {
  writeLines(paste0("Total Runtime: ", r$total, "us"))
}

get_suite_from_name <- function(benchmark_name) {
  filename <- paste0(tolower(benchmark_name), ".R")
  p <- source(filename)
  p$value
}

process_arguments <- function(args, run) {
  run$name <- args[1]
  run$benchmark_suite <- get_suite_from_name(args[1])
  
  if (length(args) > 1) {
    run$num_iterations <- strtoi(args[2])
    if (length(args) > 2) {
      run$inner_iterations <- strtoi(args[3])
    }
  }
  run
}

print_usage <- function() {
  writeLines("Harness [benchmark] [num-iterations [inner-iter]]")
  writeLines("")
  writeLines("  benchmark      - benchmark class name ")
  writeLines("  num-iterations - number of times to execute benchmark, default: 1")
  writeLines("  inner-iter     - number of times the benchmark is executed in an inner loop, ")
  writeLines("                   which is measured in total, default: 1")
}


args <- commandArgs(trailingOnly = TRUE)
args <- c("NBody")
if (length(args) < 1) {
  print_usage()
  quit(status = 1)
}

run <- Run()
run <- process_arguments(args, run)
run <- run_benchmark(run)
print_total(run)
