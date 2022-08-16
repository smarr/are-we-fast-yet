# frozen_string_literal: true

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

class Run
  attr_accessor :name, :benchmark_suite, :num_iterations, :inner_iterations

  def initialize(name)
    @name             = name
    @benchmark_suite  = load_benchmark_suite(name)
    @total            = 0
    @num_iterations   = 1
    @inner_iterations = 1
  end

  def load_benchmark_suite(benchmark_name)
    if File.exist?("#{File.dirname(__FILE__)}/#{benchmark_name.downcase}.rb")
      benchmark_file = benchmark_name.downcase
    else
      # fallback, for benchmark files that use
      # Ruby naming conventions instead of classic names
      benchmark_file = benchmark_name.gsub(/([a-z])([A-Z])/) { "#{$1}-#{$2}" }.downcase
    end
    unless require_relative(benchmark_file)
      raise "#{benchmark_file} was already loaded"
    end
    Object.const_get(benchmark_name)
  end

  def run_benchmark
    @total = 0
    puts "Starting #{@name} benchmark ..."
    do_runs(@benchmark_suite.new)
    report_benchmark
    puts ''
  end

  if RUBY_ENGINE != 'rbx' # not Rubinius
    def measure(bench)
      start_time = Process.clock_gettime(Process::CLOCK_MONOTONIC, :nanosecond)
      unless bench.inner_benchmark_loop(@inner_iterations)
        raise 'Benchmark failed with incorrect result'
      end
      end_time = Process.clock_gettime(Process::CLOCK_MONOTONIC, :nanosecond)

      run_time = (end_time - start_time) / 1000
      print_result(run_time)
      @total += run_time
    end
  else
    def measure(bench)
      start_time = Time.now
      unless bench.inner_benchmark_loop(@inner_iterations)
        raise 'Benchmark failed with incorrect result'
      end
      end_time = Time.now

      run_time = ((end_time - start_time) * 1_000_000).to_i
      print_result(run_time)
      @total += run_time
    end
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
