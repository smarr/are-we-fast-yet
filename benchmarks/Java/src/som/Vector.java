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

import java.util.Arrays;
import java.util.Comparator;

/**
 * Porting notes:
 *  - does not use an explicit array bounds check, because Java already does
 *    that. Don't see a point in doing it twice.
 */
public class Vector<E> {
  private Object[] storage;
  private int firstIdx;
  private int lastIdx;

  public static <E> Vector<E> with(final E elem) {
    Vector<E> v = new Vector<E>(1);
    v.append(elem);
    return v;
  }

  public Vector(final int size) {
    storage = new Object[size];
  }

  public Vector() {
    this(50);
  }

  @SuppressWarnings("unchecked")
  public E at(final int idx) {
    if (idx >= storage.length) {
      return null;
    }
    return (E) storage[idx];
  }

  public void atPut(final int idx, final E val) {
    if (idx >= storage.length) {
      int newLength = storage.length;
      while (newLength <= idx) {
        newLength *= 2;
      }
      storage = Arrays.copyOf(storage, newLength);
    }
    storage[idx] = val;
    if (lastIdx < idx + 1) {
      lastIdx = idx + 1;
    }
  }

  public void append(final E elem) {
    if (lastIdx >= storage.length) {
      // Need to expand capacity first
      storage = Arrays.copyOf(storage, 2 * storage.length);
    }

    storage[lastIdx] = elem;
    lastIdx++;
  }

  public boolean isEmpty() {
    return lastIdx == firstIdx;
  }

  @SuppressWarnings("unchecked")
  public void forEach(final ForEachInterface<E> fn) {
    for (int i = firstIdx; i < lastIdx; i++) {
      fn.apply((E) storage[i]);
    }
  }

  @SuppressWarnings("unchecked")
  public boolean hasSome(final TestInterface<E> fn) {
    for (int i = firstIdx; i < lastIdx; i++) {
      if (fn.test((E) storage[i])) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public E getOne(final TestInterface<E> fn) {
    for (int i = firstIdx; i < lastIdx; i++) {
      E e = (E) storage[i];
      if (fn.test(e)) {
        return e;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public E first() {
    if (isEmpty()) {
      return null;
    }
    return (E) storage[firstIdx];
  }

  @SuppressWarnings("unchecked")
  public E removeFirst() {
    if (isEmpty()) {
      return null;
    }
    firstIdx++;
    return (E) storage[firstIdx - 1];
  }

  public boolean remove(final E obj) {
    Object[] newArray = new Object[capacity()];
    int[] newLast     = new int[] {0};
    boolean[] found   = new boolean[] {false};

    forEach(it -> {
      if (it == obj) {
        found[0] = true;
      } else {
        newArray[newLast[0]] = it;
        newLast[0]++;
    }});

    storage  = newArray;
    lastIdx  = newLast[0];
    firstIdx = 0;
    return found[0];
  }

  public void removeAll() {
    firstIdx = 0;
    lastIdx = 0;
    storage = new Object[storage.length];
  }

  public int size() {
    return lastIdx - firstIdx;
  }

  public int capacity() {
    return storage.length;
  }

  public void sort(final Comparator<E> c) {
    if (size() > 0) {
      sort(firstIdx, lastIdx - 1, c);
    }
  }

  @SuppressWarnings("unchecked")
  private void sort(final int i, final int j, final Comparator<E> c) {
    if (c == null) {
      defaultSort(i, j);
    }

    int n = j + 1 - i;
    if (n <= 1) {
      return;
    }

    E di = (E) storage[i];
    E dj = (E) storage[j];

    if (c.compare(di, dj) > 0) {
      swap(storage, i, j);
      E tt = di;
      di = dj;
      dj = tt;
    }

    if (n > 2) {
      int ij = (i + j) / 2;
      E dij = (E) storage[ij];

      if (c.compare(di, dij) <= 0) {
        if (c.compare(dij, dj) > 0) {
          swap(storage, j, ij);
          dij = dj;
        }
      } else {
        swap(storage, i, ij);
        dij = di;
      }

      if (n > 3) {
        int k = i;
        int l = j - 1;

        while (true) {
          while (k <= l && c.compare(dij, (E) storage[l]) <= 0) {
            l -= 1;
          }

          k += 1;
          while (k <= l && c.compare((E) storage[k], dij) <= 0) {
            k += 1;
          }

          if (k > l) {
            break;
          }
          swap(storage, k, l);
        }

        sort(i, l, c);
        sort(k, j, c);
      }
    }
  }

  private static void swap(final Object[] storage2, final int i, final int j) {
    throw new NotImplemented();
  }

  private void defaultSort(final int i, final int j) {
    throw new NotImplemented();
  }
}
