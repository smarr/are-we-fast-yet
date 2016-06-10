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
import java.util.Arrays;

import som.Random;


public final class Storage extends Benchmark {

  private int count;

  @Override
  public Object benchmark() {
    Random random = new Random();
    count = 0;
    buildTreeDepth(7, random);
    return count;
  }

  private Object buildTreeDepth(final int depth, final Random random) {
    count++;
    if (depth == 1) {
      return new Object[random.next() % 10 + 1];
    } else {
      Object[] arr = new Object[4];
      Arrays.setAll(arr, v -> buildTreeDepth(depth - 1, random));
      return arr;
    }
  }

  @Override
  public boolean verifyResult(final Object result) {
    return 5461 == (int) result;
  }
}
