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
    for (Pair<K, V> p : pairs) {
      if (p.getKey() == key) {
        return p.getValue();
      }
    }
    return null;
  }

  private Pair<K, V> pairAt(final K key) {
    for (Pair<K, V> p : pairs) {
      if (p.getKey() == key) {
        return p;
      }
    }
    return null;
  }

  public Vector<K> getKeys() {
    return pairs.collect(p -> p.getKey());
  }
}
