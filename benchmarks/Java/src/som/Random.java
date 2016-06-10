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
package som;

public class Random {
  private int seed = 74755;

  public int next() {
    seed = ((seed * 1309) + 13849) & 65535;
    return seed;
  }

  public static void main(final String[] args) {
    // Checkstyle: stop
    System.out.println("Testing random number generator ...");
    // Checkstyle: resume
    Random rnd = new Random();

    try {
      if (rnd.next() != 22896) { throw new RuntimeException(); }
      if (rnd.next() != 34761) { throw new RuntimeException(); }
      if (rnd.next() != 34014) { throw new RuntimeException(); }
      if (rnd.next() != 39231) { throw new RuntimeException(); }
      if (rnd.next() != 52540) { throw new RuntimeException(); }
      if (rnd.next() != 41445) { throw new RuntimeException(); }
      if (rnd.next() !=  1546) { throw new RuntimeException(); }
      if (rnd.next() !=  5947) { throw new RuntimeException(); }
      if (rnd.next() != 65224) { throw new RuntimeException(); }
    } catch (RuntimeException e) {
      // Checkstyle: stop
      System.err.println("FAILED");
      // Checkstyle: resume
      return;
    }
  }
}
