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
public final class List extends Benchmark {

  private static class Element {
    private Object val;
    private Element next;

    Element(final Object v) {
      val = v;
    }

    public int length() {
      if (next == null) {
        return 1;
      } else {
        return 1 + next.length();
      }
    }

    public Object  getVal()                 { return val; }
    public void    setVal(final Object v)   { val = v; }
    public Element getNext()                { return next; }
    public void    setNext(final Element e) { next = e; }
  }

  @Override
  public Object benchmark() {
    Element result = tail(makeList(15), makeList(10), makeList(6));
    return result.length();
  }

  public Element makeList(final int length) {
    if (length == 0) { return null; } else {
      Element e = new Element(length);
      e.setNext(makeList(length - 1));
      return e;
    }
  }

  public boolean isShorterThan(final Element x, final Element y) {
    Element xTail = x;
    Element yTail = y;

    while (yTail != null) {
      if (xTail == null) { return true; }
      xTail = xTail.getNext();
      yTail = yTail.getNext();
    }
    return false;
  }

  public Element tail(final Element x, final Element y, final Element z) {
    if (isShorterThan(y, x)) {
      return tail(tail(x.getNext(), y, z),
             tail(y.getNext(), z, x),
             tail(z.getNext(), x, y));
    } else {
      return z;
    }
  }

  @Override
  public boolean verifyResult(final Object result) {
    return 10 == (int) result;
  }
}
