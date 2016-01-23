# Copyright (c) 2015 Stefan Marr <git@stefan-marr.de>
#
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

class Run
  property :name
  property :benchmark_suite
  property :num_iterations
  property :inner_iterations

  def initialize
    @total            = 0
    @num_iterations   = 1
    @inner_iterations = 1
    @benchmark_suite  = TheBenchmark
    @name             = nil
  end

  def run_benchmark
    puts "Starting #{@name} benchmark ..."
    do_runs(@benchmark_suite.new)
    report_benchmark
    puts ""
  end

  def measure(bench)
    start_time = Time.now # Process.clock_gettime(Process::CLOCK_MONOTONIC, :nanosecond)
    unless bench.inner_benchmark_loop(@inner_iterations)
      raise "Benchmark failed with incorrect result"
    end
    end_time = Time.now # Process.clock_gettime(Process::CLOCK_MONOTONIC, :nanosecond)

    run_time = (end_time - start_time).to_f
    print_result(run_time)
    @total += run_time
  end

  def do_runs(bench)
    @num_iterations.times { measure(bench) }
  end

  def report_benchmark
    puts "#{@name}: iterations=#{@num_iterations} average: #{@total / @num_iterations}us total: #{@total}us\n"
  end

  def print_result(run_time)
    puts "#{@name}: iterations=1 runtime: #{run_time}us"
  end

  def print_total
    puts "Total Runtime: #{@total}us"
  end
end

def process_arguments(args, run)
  run.name = args[0]

  if args.size > 1
    run.num_iterations = args[1].to_i
    if args.size > 2
      run.inner_iterations = args[2].to_i
    end
  end
end

def print_usage
  puts "./harness.rb [benchmark] [num-iterations [inner-iter]]"
  puts ""
  puts "  benchmark      - benchmark class name "
  puts "  num-iterations - number of times to execute benchmark, default: 1"
  puts "  inner-iter     - number of times the benchmark is executed in an inner loop, "
  puts "                   which is measured in total, default: 1"
end

if ARGV.size < 1
  print_usage
  exit 1
end

run = Run.new
process_arguments(ARGV, run)
run.run_benchmark
run.print_total
