#!/usr/bin/env julia
# Copyright (c) 2021 Valentin Churavy <v.churavy@gmail.com>

# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the 'Software'), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.

mutable struct Run
    name::String
    benchmark::Module
    total::Float64
    num_iterations::Int
    inner_iterations::Int
end

function Run(name, num_iterations, inner_iterations)
    benchmark = Module()
    Base.include(benchmark, "$name.jl")
    Run(name, benchmark, 0.0, num_iterations, inner_iterations)
end

function measure!(run::Run)
    start_time = time()
    @assert run.benchmark.inner_benchmark_loop(run.inner_iterations) "Benchmark failed with incorrect result"
    end_time = time()

    run_time = end_time - start_time
    println("$(run.name): iterations=1 runtime: $(run_time)s")
    run.total += run_time
end


function (run::Run)()
    println(("Starting $(run.name) benchmark ..."))
    for _ in 1:run.num_iterations
        measure!(run)
    end
    println("$(run.name): iterations=$(run.num_iterations) average: $(run.total/run.num_iterations)s total: $(run.total)s")
end

if !(0 < length(ARGS) <= 3)
    println("Usage: ./harness.jl <benchmark_name> [num_iterations] [inner_iterations]")
    exit(1)
end

num_iterations = length(ARGS) >= 2 ? parse(Int, ARGS[2]) : 1
inner_iterations = length(ARGS) >= 3 ? parse(Int, ARGS[3]) : 1
let run = Run(ARGS[1], num_iterations, inner_iterations)
    run()
end