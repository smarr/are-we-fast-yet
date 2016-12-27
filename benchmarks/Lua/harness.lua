#!/usr/bin/env lua
-- This code is derived from the SOM benchmarks, see AUTHORS.md file.
--
-- Copyright (c) 2016 Francois Perrad <francois.perrad@gadz.org>
--
-- Permission is hereby granted, free of charge, to any person obtaining a copy
-- of this software and associated documentation files (the 'Software'), to deal
-- in the Software without restriction, including without limitation the rights
-- to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
-- copies of the Software, and to permit persons to whom the Software is
-- furnished to do so, subject to the following conditions:
--
-- The above copyright notice and this permission notice shall be included in
-- all copies or substantial portions of the Software.
--
-- THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
-- IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
-- FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
-- AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
-- LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
-- OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
-- THE SOFTWARE.

--[[
    os.clock() wraps the C clock() / CLOCKS_PER_SEC
    socket.gettime() wraps the POSIX gettimeofday() with a microsecond resolution
]]
local ok, socket = pcall(require, 'socket')
local gettime = ok and socket.gettime or os.clock

local run = {} do

function run:init (name, num_iterations, inner_iterations)
    self.name             = name
    self.benchmark        = require(name:lower())
    self.total            = 0
    self.num_iterations   = tonumber(num_iterations)
    self.inner_iterations = tonumber(inner_iterations)
end

function run:run_benchmark ()
    print(("Starting %s benchmark ..."):format(self.name))
    self:do_runs()
    self:report_benchmark()
end

function run:measure ()
    local start_time = gettime()
    assert(self.benchmark:inner_benchmark_loop(self.inner_iterations),
           'Benchmark failed with incorrect result')
    local end_time = gettime()

    local run_time = (end_time - start_time) * 1.0e6
    self:print_result(run_time)
    self.total = self.total + run_time
end

function run:do_runs ()
    for _ = 1, self.num_iterations do
        self:measure()
    end
end

function run:report_benchmark ()
    print(("%s: iterations=%d average: %.0fus total: %.0fus\n"):format(
        self.name, self.num_iterations, self.total / self.num_iterations, self.total))
end

function run:print_result (run_time)
    print(("%s: iterations=1 runtime: %.0fus"):format(self.name, run_time))

end

function run:print_total ()
    print(("Total Runtime: %.0fus"):format(self.total))
end

end -- object run

local function print_usage ()
    print [==[
./harness.lua benchmark [num-iterations [inner-iter]]

  benchmark      - benchmark class name
  num-iterations - number of times to execute benchmark, default: 1
  inner-iter     - number of times the benchmark is executed in an inner loop,
                   which is measured in total, default: 1
]==]
end

if #arg < 1 then
    print_usage()
    os.exit(1)
end

run:init(arg[1], arg[2] or 1, arg[3] or 1)
run:run_benchmark()
run:print_total()
