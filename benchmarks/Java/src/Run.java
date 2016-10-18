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
public final class Run {
  private final String name;
  private final Class<? extends Benchmark> benchmarkSuite;
  private int numIterations;
  private int innerIterations;
  private long total;

  public Run(final String name) {
    this.name = name;
    this.benchmarkSuite = getSuiteFromName(name);
    numIterations   = 1;
    innerIterations = 1;
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends Benchmark> getSuiteFromName(final String name) {
    try {
      return (Class<? extends Benchmark>) Class.forName(name);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public void runBenchmark() {
    // Checkstyle: stop
    System.out.println("Starting " + name + " benchmark ...");
    // Checkstyle: resume

    try {
      doRuns(benchmarkSuite.newInstance());
    } catch (ReflectiveOperationException | IllegalArgumentException
        | SecurityException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    reportBenchmark();

    // Checkstyle: stop
    System.out.println();
    // Checkstyle: resume
  }

  private void measure(final Benchmark bench) {
    long startTime = System.nanoTime();
    if (!bench.innerBenchmarkLoop(innerIterations)) {
      throw new RuntimeException("Benchmark failed with incorrect result");
    }
    long endTime = System.nanoTime();
    long runTime = (endTime - startTime) / 1000;

    printResult(runTime);

    total += runTime;
  }

  private void doRuns(final Benchmark bench) {
    for (int i = 0; i < numIterations; i++) {
      measure(bench);
    }
  }

  private void reportBenchmark() {
    // Checkstyle: stop
    System.out.println(name + ": iterations=" + numIterations +
        " average: " + (total / numIterations) + "us total: " + total + "us\n");
    // Checkstyle: resume
  }

  private void printResult(final long runTime) {
    // Checkstyle: stop
    System.out.println(name + ": iterations=1 runtime: " + runTime + "us");
    // Checkstyle: resume
  }

  public void printTotal() {
    // Checkstyle: stop
    System.out.println("Total Runtime: " + total + "us");
    // Checkstyle: resume
  }

  public void setNumIterations(final int numIterations) {
    this.numIterations = numIterations;
  }

  public void setInnerIterations(final int innerIterations) {
    this.innerIterations = innerIterations;
  }
}
