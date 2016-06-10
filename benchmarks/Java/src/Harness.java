/* This code is based on the SOM class library.
 *
 * Copyright (c) 2001-2016 see AUTHORS.md file
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the 'Software'), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public final class Harness {

  private static Run processArguments(final String[] args) {
    Run run = new Run(args[0]);

    if (args.length > 1) {
      run.setNumIterations(Integer.valueOf(args[1]));
      if (args.length > 2) {
        run.setInnerIterations(Integer.valueOf(args[2]));
      }
    }

    return run;
  }

  private static void printUsage() {
    // Checkstyle: stop
    System.out.println("Harness [benchmark] [num-iterations [inner-iter]]");
    System.out.println();
    System.out.println("  benchmark      - benchmark class name ");
    System.out.println("  num-iterations - number of times to execute benchmark, default: 1");
    System.out.println("  inner-iter     - number of times the benchmark is executed in an inner loop, ");
    System.out.println("                   which is measured in total, default: 1");
    // Checkstyle: resume
  }

  public static void main(final String[] args) {
    if (args.length < 2) {
      printUsage();
      System.exit(1);
    }

    Run run = processArguments(args);
    run.runBenchmark();
    run.printTotal();
  }
}
