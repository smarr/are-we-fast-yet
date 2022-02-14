# This code is based on the SOM class library.
#
# Copyright (c) 2001-2021 see AUTHORS.md file
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
import sys

from run import Run


def process_arguments(args):
    new_run = Run(args[1])

    if len(args) > 2:
        new_run.set_num_iterations(int(args[2]))
        if len(args) > 3:
            new_run.set_inner_iterations(int(args[3]))

    return new_run


def print_usage():
    print("./harness.py [benchmark] [num-iterations [inner-iter]]")
    print()
    print("  benchmark      - benchmark class name ")
    print("  num-iterations - number of times to execute benchmark, default: 1")
    print(
        "  inner-iter     - number of times the benchmark is executed in an inner loop, "
    )
    print("                   which is measured in total, default: 1")


if len(sys.argv) < 2:
    print_usage()
    sys.exit(1)

run = process_arguments(sys.argv)
run.run_benchmark()
run.print_total()
