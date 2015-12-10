import deltablue.Planner;

public class DeltaBlue extends Benchmark {

  @Override
  public boolean innerBenchmarkLoop(final int innerIterations) {
    Planner.chainTest(innerIterations);
    Planner.projectionTest(innerIterations);
    return true;
  }

  @Override
  public Object benchmark() {
    throw new UnsupportedOperationException("should never be reached");
  }

  @Override
  public boolean verifyResult(final Object result) {
    throw new UnsupportedOperationException("should never be reached");
  }
}
