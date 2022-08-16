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

require_relative 'run'

def process_arguments(args)
  run = Run.new(args[0])

  if args.size > 1
    run.num_iterations = Integer(args[1])
    run.inner_iterations = Integer(args[2]) if args.size > 2
  end
  run
end

def print_usage
  puts './harness.rb [benchmark] [num-iterations [inner-iter]]'
  puts ''
  puts '  benchmark      - benchmark class name '
  puts '  num-iterations - number of times to execute benchmark, default: 1'
  puts '  inner-iter     - number of times the benchmark is executed in an inner loop, '
  puts '                   which is measured in total, default: 1'
end

if ARGV.size < 1
  print_usage
  exit 1
end

run = process_arguments(ARGV)
run.run_benchmark
run.print_total
