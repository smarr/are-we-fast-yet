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
import havlak.LoopTesterApp;

public final class Havlak extends Benchmark {

  @Override
  public boolean innerBenchmarkLoop(final int innerIterations) {
    return verifyResult((new LoopTesterApp()).main(
        innerIterations, 50, 10 /* was 100 */, 10, 5), innerIterations);
  }

  public boolean verifyResult(final Object result, final int innerIterations) {
    int[] r = (int[]) result;

    if (innerIterations == 15000) { return r[0] == 46602 && r[1] == 5213; }
    if (innerIterations ==  1500) { return r[0] ==  6102 && r[1] == 5213; }
    if (innerIterations ==   150) { return r[0] ==  2052 && r[1] == 5213; }
    if (innerIterations ==    15) { return r[0] ==  1647 && r[1] == 5213; }
    if (innerIterations ==     1) { return r[0] ==  1605 && r[1] == 5213; }

    // Checkstyle: stop
    System.out.println("No verification result for " + innerIterations + " found");
    System.out.println("Result is: " + r[0] + ", " + r[1]);
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
