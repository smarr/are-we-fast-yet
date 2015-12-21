

import richards.Scheduler;

/*
 * This version is a port of the SOM Richards benchmark to Java.
 * It is kept as close to the SOM version as possible, for cross-language
 * benchmarking.
 */

public class Richards extends Benchmark {

  @Override
  public Object benchmark() {
    return (new Scheduler()).start();
  }

  @Override
  public boolean verifyResult(final Object result) {
    return (boolean) result;
  }
}
