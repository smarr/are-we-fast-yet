import nbody.NBodySystem;

public final class NBody extends Benchmark {

  @Override
  public boolean innerBenchmarkLoop(final int innerIterations) {
    NBodySystem system = new NBodySystem();
    for (int i = 0; i < innerIterations; i++) {
      system.advance(0.01);
    }

    return verifyResult(system.energy(), innerIterations);
  }

  private boolean verifyResult(final double result, final int innerIterations) {
    if (innerIterations == 250000) {
      return result == -0.1690859889909308;
    }

    System.out.println("No verification result for " + innerIterations + " found");
    System.out.println("Result is: " + result);
    return false;
  }

  @Override
  public Object benchmark() {
    throw new RuntimeException("Should never be reached");
  }

  @Override
  public boolean verifyResult(final Object result) {
    throw new RuntimeException("Should never be reached");
  }
}
