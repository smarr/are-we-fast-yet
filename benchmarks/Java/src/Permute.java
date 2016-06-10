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
public final class Permute extends Benchmark {
  private int count;
  private int[] v;

  @Override
  public Object benchmark() {
    count = 0;
    v     = new int[6];
    permute(6);
    return count;
  }

  void permute(final int n) {
    count++;
    if (n != 0) {
      int n1 = n - 1;
      permute(n1);
      for (int i = n1; i >= 0; i--) {
        swap(n1, i);
        permute(n1);
        swap(n1, i);
      }
    }
  }

  private void swap(final int i, final int j) {
    int tmp = v[i];
    v[i] = v[j];
    v[j] = tmp;
  }

  @Override
  public boolean verifyResult(final Object result) {
    return (int) result == 8660;
  }

}
