/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted basde on the SOM version.
 */
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
    if (innerIterations == 10000000) {
      return result == -0.1690778416543499;
    }
    if (innerIterations == 1000000) {
      return result == -0.16908618459850192;
    }
    if (innerIterations == 250000) {
      return result == -0.1690859889909308;
    }
    if (innerIterations == 160000) {
      return result == -0.16901416322875248;
    }
    if (innerIterations == 16000) {
      return result == -0.16902211820216737;
    }
    if (innerIterations == 1600) {
      return result == -0.16901124420350946;
    }
    if (innerIterations == 1) {
      return result == -0.16907495402506745;
    }

    // Checkstyle: stop
    System.out.println("No verification result for " + innerIterations + " found");
    System.out.println("Result is: " + result);
    // Checkstyle: resume
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
