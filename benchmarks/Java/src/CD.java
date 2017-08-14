/*
 * Copyright (c) 2001-2016 Stefan Marr
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
import cd.Collision;
import cd.CollisionDetector;
import cd.Simulator;
import som.Vector;

public final class CD extends Benchmark {

  private int benchmark(final int numAircrafts) {
    int numFrames = 200;

    Simulator simulator = new Simulator(numAircrafts);
    CollisionDetector detector = new CollisionDetector();

    int actualCollisions = 0;

    for (int i = 0; i < numFrames; i++) {
      double time = i / 10.0;
      Vector<Collision> collisions = detector.handleNewFrame(simulator.simulate(time));
      actualCollisions += collisions.size();
    }

    return actualCollisions;
  }

  @Override
  public boolean innerBenchmarkLoop(final int innerIterations) {
    return verifyResult(benchmark(innerIterations), innerIterations);
  }

  public boolean verifyResult(final int actualCollisions, final int numAircrafts) {
    if (numAircrafts == 1000) { return actualCollisions == 14484; }
    if (numAircrafts ==  500) { return actualCollisions == 14484; }
    if (numAircrafts ==  250) { return actualCollisions == 10830; }
    if (numAircrafts ==  200) { return actualCollisions ==  8655; }
    if (numAircrafts ==  100) { return actualCollisions ==  4305; }
    if (numAircrafts ==   10) { return actualCollisions ==   390; }

    // Checkstyle: stop
    System.out.println("No verification result for " + numAircrafts + " found");
    System.out.println("Result is: " + actualCollisions);
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
