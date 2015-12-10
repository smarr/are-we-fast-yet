package som;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Porting notes:
 *  - does not use an explicit array bounds check, because Java already does
 *    that. Don't see a point in doing it twice.
 */
public class Vector<E> implements Iterable<E> {
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
    return (E) storage[idx];
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
  public E removeFirst() {
    if (isEmpty()) {
      throw new ArrayIndexOutOfBoundsException();
    }
    firstIdx++;
    return (E) storage[firstIdx - 1];
  }

  public boolean remove(final E obj) {
    Object[] newArray = new Object[capacity()];
    int newLast = 0;
    boolean found = false;

    for (E it : this) {
      if (it == obj) {
        found = true;
      } else {
        newArray[newLast] = it;
        newLast++;
      }
    }

    storage  = newArray;
    lastIdx  = newLast;
    firstIdx = 0;
    return found;
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

  private class VectorIterator implements Iterator<E> {

    private int current;

    VectorIterator() {
      current = firstIdx;
    }

    @Override
    public boolean hasNext() {
      return current < lastIdx;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E next() {
      current++;
      return (E) storage[current - 1];
    }
  }

  @Override
  public Iterator<E> iterator() {
    return new VectorIterator();
  }
}
