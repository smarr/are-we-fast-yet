public final class Run {
  private String name;
  private Class<? extends Benchmark> benchmarkSuite;
  private int numIterations;
  private int innerIterations;
  private long total;

  public Run() {
    numIterations   = 1;
    innerIterations = 1;
  }

  public void runBenchmark() {
    System.out.println("Starting " + name + " benchmark ...");
    try {
      doRuns(benchmarkSuite.newInstance());
    } catch (ReflectiveOperationException | IllegalArgumentException
        | SecurityException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    reportBenchmark();
    System.out.println();
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
    System.out.println(name + ": iterations=" + numIterations +
        " average: " + (total / numIterations) + "us total: " + total + "us\n");
  }

  private void printResult(final long runTime) {
    System.out.println(name + ": iterations=1 runtime: " + runTime + "us");
  }

  public void printTotal() {
    System.out.println("Total Runtime: " + total + "us");
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public Class<? extends Benchmark> getBenchmarkSuite() {
    return benchmarkSuite;
  }

  public void setBenchmarkSuite(final Class<? extends Benchmark> suite) {
    benchmarkSuite = suite;
  }

  public int getNumIterations() {
    return numIterations;
  }

  public void setNumIterations(final int numIterations) {
    this.numIterations = numIterations;
  }

  public int getInnerIterations() {
    return innerIterations;
  }

  public void setInnerIterations(final int innerIterations) {
    this.innerIterations = innerIterations;
  }
}
