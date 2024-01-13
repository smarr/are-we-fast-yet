# This code is derived from the SOM benchmarks, see AUTHORS.md file.
#
# Copyright (c) 2023 Valentin Churavy <v.churavy@gmail.com>

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

module Storage

include(joinpath(@__DIR__, "benchmark.jl"))
include(joinpath(@__DIR__, "som", "random.jl"))


mutable struct Benchmark
    _count::Int
    Benchmark() = new(0)
end

function benchmark(self::Benchmark)
    random = Random()
    self._count = 0
    build_tree_depth(self, 7, random)

    return self._count
end

function build_tree_depth(self::Benchmark, depth, random)
    self._count += 1
    if depth == 1
        return Any[nothing for _ in 1:(next!(random) % 10 + 1)]
    end

    arr = Any[nothing for _ in 1:4]
    for i in eachindex(arr)
        arr[i] = build_tree_depth(self, depth - 1, random)
    end
    return arr
end

function verify_result(::Benchmark, result)
    result == 5461
end

end # module
