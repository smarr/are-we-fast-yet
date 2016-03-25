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


public final class Dictionary<K, V> {
  private final IdentitySet<Pair<K, V>> pairs;

  public Dictionary(final int size) {
    pairs = new IdentitySet<>(size);
  }

  public Dictionary() {
    this(Constants.INITIAL_SIZE);
  }

  public void atPut(final K key, final V value) {
    Pair<K, V> pair = pairAt(key);
    if (pair == null) {
      pairs.add(new Pair<>(key, value));
    } else {
      pair.setValue(value);
    }
  }

  public V at(final K key) {
    Pair<K, V> pair = pairAt(key);
    if (pair == null) {
      return null;
    } else {
      return pair.getValue();
    }
  }

  private Pair<K, V> pairAt(final K key) {
    return pairs.getOne(p -> {return p.getKey() == key; });
  }

  public Vector<K> getKeys() {
    return pairs.collect(p -> p.getKey());
  }
}
