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

import som.Dictionary.CustomHash;


public class Dictionary<K extends CustomHash, V> {

  public interface CustomHash {
    int customHash();
  }

  protected static final int INITIAL_CAPACITY = 16;

  private Entry<K, V>[] buckets;
  private int          size;

  static class Entry<K, V> {

    final int   hash;
    final K     key;
    V           value;
    Entry<K, V> next;

    Entry(final int hash, final K key, final V value, final Entry<K, V> next) {
      this.hash  = hash;
      this.key   = key;
      this.value = value;
      this.next  = next;
    }

    boolean match(final int hash, final K key) {
      return this.hash == hash && key.equals(this.key);
    }
  }

  @SuppressWarnings("unchecked")
  public Dictionary(final int size) {
    this.buckets = new Entry[size];
  }

  public Dictionary() {
    this(INITIAL_CAPACITY);
  }

  private static <K extends CustomHash> int hash(final K key) {
    if (key == null) {
      return 0;
    }
    int hash = key.customHash();
    return hash ^ hash >>> 16;
  }

  public int size() {
    return size;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  private int getBucketIdx(final int hash) {
    return (buckets.length - 1) & hash;
  }

  private Entry<K, V> getBucket(final int hash) {
    return buckets[getBucketIdx(hash)];
  }

  public V at(final K key) {
    int hash = hash(key);
    Entry<K, V> e = getBucket(hash);

    while (e != null) {
      if (e.match(hash, key)) {
        return e.value;
      }
      e = e.next;
    }
    return null;
  }

  public boolean containsKey(final K key) {
    int hash = hash(key);
    Entry<K, V> e = getBucket(hash);

    while (e != null) {
      if (e.match(hash, key)) {
        return true;
      }
      e = e.next;
    }
    return false;
  }

  public void atPut(final K key, final V value) {
    int hash = hash(key);
    int i = getBucketIdx(hash);

    Entry<K, V> current = buckets[i];

    if (current == null) {
      buckets[i] = newEntry(key, value, hash);
      size += 1;
    } else {
      insertBucketEntry(key, value, hash, current);
    }

    if (size > buckets.length) {
      resize();
    }
  }

  protected Entry<K, V> newEntry(final K key, final V value, final int hash) {
    return new Entry<>(hash, key, value, null);
  }

  private void insertBucketEntry(final K key,
      final V value, final int hash, final Entry<K, V> head) {
    Entry<K, V> current = head;

    while (true) {
      if (current.match(hash, key)) {
        current.value = value;
        return;
      }
      if (current.next == null) {
        size += 1;
        current.next = newEntry(key, value, hash);
        return;
      }
      current = current.next;
    }
  }

  private void resize() {
    Entry<K, V>[] oldStorage = buckets;

    @SuppressWarnings("unchecked")
    Entry<K, V>[] newStorage = new Entry[oldStorage.length * 2];
    buckets = newStorage;
    transferEntries(oldStorage);
  }

  private void transferEntries(final Entry<K, V>[] oldStorage) {
    for (int i = 0; i < oldStorage.length; ++i) {
      Entry<K, V> current = oldStorage[i];
      if (current != null) {
        oldStorage[i] = null;

        if (current.next == null) {
          buckets[current.hash & (buckets.length - 1)] = current;
        } else {
          splitBucket(oldStorage, i, current);
        }
      }
    }
  }

  private void splitBucket(final Entry<K, V>[] oldStorage, final int i,
      final Entry<K, V> head) {
    Entry<K, V> loHead = null;
    Entry<K, V> loTail = null;
    Entry<K, V> hiHead = null;
    Entry<K, V> hiTail = null;
    Entry<K, V> current = head;

    while (current != null) {
      if ((current.hash & oldStorage.length) == 0) {
        if (loTail == null) {
          loHead = current;
        } else {
          loTail.next = current;
        }
        loTail = current;
      } else {
        if (hiTail == null) {
          hiHead = current;
        } else {
          hiTail.next = current;
        }
        hiTail = current;
      }
      current = current.next;
    }

    if (loTail != null) {
      loTail.next = null;
      buckets[i] = loHead;
    }
    if (hiTail != null) {
      hiTail.next = null;
      buckets[i + oldStorage.length] = hiHead;
    }
  }

  @SuppressWarnings("unchecked")
  public void removeAll() {
    buckets = new Entry[buckets.length];
    size = 0;
  }

  public Vector<K> getKeys() {
    Vector<K> keys = new Vector<>(size);
    for (int i = 0; i < buckets.length; ++i) {
      Entry<K, V> current = buckets[i];
      while (current != null) {
        keys.append(current.key);
        current = current.next;
      }
    }
    return keys;
  }

  public Vector<V> getValues() {
    Vector<V> values = new Vector<>(size);
    for (int i = 0; i < buckets.length; ++i) {
      Entry<K, V> current = buckets[i];
      while (current != null) {
        values.append(current.value);
        current = current.next;
      }
    }
    return values;
  }
}
