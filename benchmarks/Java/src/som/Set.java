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

public class Set<E> {
  private final Vector<E> items;

  public Set() {
    this(Constants.INITIAL_SIZE);
  }

  public Set(final int size) {
    items = new Vector<E>(size);
  }

  public int size() {
    return items.size();
  }

  public void forEach(final ForEachInterface<E> fn) {
    items.forEach(fn);
  }

  public boolean hasSome(final TestInterface<E> fn) {
    return items.hasSome(fn);
  }

  public E getOne(final TestInterface<E> fn) {
    return items.getOne(fn);
  }

  public void add(final E obj) {
    if (!contains(obj)) {
      items.append(obj);
    }
  }

  public <T> Vector<T> collect(final CollectInterface<E, T> fn) {
    Vector<T> coll = new Vector<T>();

    forEach(e -> {
      coll.append(fn.collect(e));
    });
    return coll;
  }

  public boolean contains(final E obj) {
    return hasSome(e -> { return e.equals(obj); });
  }

  public void removeAll() {
    items.removeAll();
  }
}
