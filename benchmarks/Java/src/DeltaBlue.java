/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * DeltaBlue.
 * 
 * It is modified to use the SOM class library and Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
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
