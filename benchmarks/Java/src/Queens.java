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


public class Queens extends Benchmark {

  private boolean[] freeMaxs;
  private boolean[] freeRows;
  private boolean[] freeMins;
  private int[]     queenRows;

  @Override
  public Object benchmark() {
    boolean result = true;
    for (int i = 0; i < 10; i++) {
      result = result && queens();
    }
    return result;
  }

  private boolean queens() {
    freeRows  = new boolean[ 8]; Arrays.fill(freeRows, true);
    freeMaxs  = new boolean[16]; Arrays.fill(freeMaxs, true);
    freeMins  = new boolean[16]; Arrays.fill(freeMins, true);
    queenRows = new     int[ 8]; Arrays.fill(queenRows, -1);

    return placeQueen(0);
  }

  boolean placeQueen(final int c) {
    for (int r = 0; r < 8; r++) {
      if (getRowColumn(r, c)) {
        queenRows[r] = c;
        setRowColumn(r, c, false);

        if (c == 7) {
          return true;
        }

        if (placeQueen(c + 1)) {
          return true;
        }
        setRowColumn(r, c, true);
      }
    }
    return false;
  }

  boolean getRowColumn(final int r, final int c) {
    return freeRows[r] && freeMaxs[c + r] && freeMins[c - r + 7];
  }

  void setRowColumn(final int r, final int c, final boolean v) {
    freeRows[r        ] = v;
    freeMaxs[c + r    ] = v;
    freeMins[c - r + 7] = v;
  }

  @Override
  public boolean verifyResult(final Object result) {
    return (boolean) result;
  }
}
