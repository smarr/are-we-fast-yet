# Copyright (c) 2015-2016 Stefan Marr <git@stefan-marr.de>
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

require "./bounce"
require "./cd"
require "./deltablue"
require "./havlak"
require "./json"
require "./list"
require "./mandelbrot"
require "./nbody"
require "./permute"
require "./queens"
require "./richards"
require "./sieve"
require "./storage"
require "./towers"

class Run
  property :name
  property :benchmark
  property :num_iterations
  property :inner_iterations
  
  @benchmark : Benchmark

  def initialize(name : String)
    @total            = 0
    @num_iterations   = 1
    @inner_iterations = 1
    @benchmark        = select_benchmark(name)
    @name             = name
  end

  def select_benchmark(benchmark_name)
    case benchmark_name
    when "Bounce"
      Bounce.new
    when "CD"
      CD.new
    when "DeltaBlue"
      DeltaBlue.new
    when "Havlak"
      Havlak.new
    when "Json"
      Json.new
    when "List"
      List.new
    when "Mandelbrot"
      Mandelbrot.new
    when "NBody"
      NBody.new
    when "Permute"
      Permute.new
    when "Queens"
      Queens.new
    when "Richards"
      Richards.new
    when "Sieve"
      Sieve.new
    when "Storage"
      Storage.new
    when "Towers"
      Towers.new
    else
      raise Exception.new("Benchmark not known: " + benchmark_name)
    end
  end

  def run_benchmark
    puts "Starting #{@name} benchmark ..."
    do_runs(@benchmark)
    report_benchmark
    puts ""
  end

  def measure(bench)
    start_time = Time.utc # Process.clock_gettime(Process::CLOCK_MONOTONIC, :nanosecond)
    unless bench.inner_benchmark_loop(@inner_iterations)
      raise "Benchmark failed with incorrect result"
    end
    end_time = Time.utc # Process.clock_gettime(Process::CLOCK_MONOTONIC, :nanosecond)

    run_time = ((end_time - start_time).total_milliseconds * 1000).to_i
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

def process_arguments(args)
  run = Run.new(args[0])

  if args.size > 1
    run.num_iterations = args[1].to_i
    if args.size > 2
      run.inner_iterations = args[2].to_i
    end
  end
  run
end

def print_usage
  puts "./harness.rb benchmark [num-iterations [inner-iter]]"
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

run = process_arguments(ARGV)
run.run_benchmark
run.print_total
