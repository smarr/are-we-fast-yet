# try to follow style guide: http://r-pkgs.had.co.nz/style.html

Benchmark <- function() {
  structure(list(), class = "Benchmark")
}

benchmark <- function(b) UseMethod("benchmark", b)
benchmark.Benchmark <- function(b) {
  stop(simpleError("subclass responsibility"))
}

verify_result <- function(b, result) UseMethod("verify_result", b)
verify_result.Benchmark <- function(b) {
  stop(simpleError("subclass responsibility"))
}

inner_benchmark_loop <- function(b, inner_iterations) UseMethod("inner_benchmark_loop", b)
inner_benchmark_loop.Benchmark <- function(b, inner_iterations) {
  for (i in 1:inner_iterations) {
    if (!verify_result(b, benchmark(b))) {
      return(FALSE);
    }
  }
  TRUE
}
