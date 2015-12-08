
public abstract class Benchmark {
  protected int innerIterations;

  public void error(final String msg) {
    throw new RuntimeException(msg);
  }

  public void run(final String[] args) {
    try {
      runWithError(args);
    } catch (RuntimeException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void runWithError(final String[] args) {
    int numIterations = Integer.valueOf(args[0]);
    int warmUp        = Integer.valueOf(args[1]);
    innerIterations   = Integer.valueOf(args[2]);

    for (int i = 0; i < warmUp; i++) {
      if (!innerBenchmarkLoop()) {
        error("Benchmark failed with incorrect result");
      }
    }

    String className = getClass().getName();
    for (int i = 0; i < numIterations; i++) {
      long start = System.nanoTime();
      if (!innerBenchmarkLoop()) {
        error("Benchmark failed with incorrect result");
      }
      long end = System.nanoTime();
      long microseconds = (end - start) / 1000;

      System.out.println(className + ": iterations=1 runtime: " + microseconds + "us");
    }
  }

  public abstract Object benchmark();
  public abstract boolean verifyResult(Object result);

  public boolean innerBenchmarkLoop() {
    for (int i = 0; i < innerIterations; i++) {
      if (!verifyResult(benchmark())) {
        return false;
      }
    }
    return true;
  }

  public boolean assertEquals(final Object expected, final Object value) {
    if (!expected.equals(value)) {
      error("Expected value (" + expected + ") differs from actual (" + value + ") benchmark result.");
    }
    return true;
  }
}
