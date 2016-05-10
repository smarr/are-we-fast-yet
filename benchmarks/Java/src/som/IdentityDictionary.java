package som;

import som.Dictionary.CustomHash;


public class IdentityDictionary<K extends CustomHash, V> extends Dictionary<K, V> {

  static class IdEntry<K, V> extends Entry<K, V> {
    IdEntry(final int hash, final K key, final V value, final Entry<K, V> next) {
      super(hash, key, value, next);
    }

    @Override
    boolean match(final int hash, final K key) {
      return this.hash == hash && this.key == key;
    }
  }

  public IdentityDictionary(final int size) {
    super(size);
  }

  public IdentityDictionary() {
    super(INITIAL_CAPACITY);
  }

  @Override
  protected Entry<K, V> newEntry(final K key, final V value, final int hash) {
    return new IdEntry<>(hash, key, value, null);
  }
}
