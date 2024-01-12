#pragma once

#include <memory>
#include "vector.h"

template <typename K, typename V>
class IdentityDictionary;

/**
 * The Dictionary class does not manage the memory of keys and values.
 *
 * The memory of Entry objects is managed by ownership through the _buckets
 * field. Thus, they are anchored in the _buckets, and only freed from there.
 */
template <typename K, typename V>
class Dictionary {
  friend class IdentityDictionary<K, V>;

 private:
  class Entry {
    friend class IdentityDictionary<K, V>;
    friend class Dictionary<K, V>;

   private:
    uint32_t _hash;
    const K* const _key;
    V _value;
    Entry* _next;

   public:
    Entry(uint32_t h, const K* k, const V& v, Entry* n)
        : _hash(h), _key(k), _value(v), _next(n) {}
    virtual ~Entry() = default;

    virtual bool match(uint32_t h, const K* const k) {
      return _hash == h && _key == k;
    }

    [[nodiscard]] const K* getKey() const { return _key; }

    [[nodiscard]] uint32_t getHash() const { return _hash; }
  };

  static const uint32_t INITIAL_CAPACITY = 16;

  Entry** _buckets;
  uint32_t _size{0};
  uint32_t _capacity;

 public:
  explicit Dictionary(uint32_t capacity = INITIAL_CAPACITY)
      : _buckets(new Entry* [capacity] {}), _capacity(capacity) {}

  virtual ~Dictionary() {
    removeAll();
    delete[] _buckets;
  }

  [[nodiscard]] uint32_t getSize() const { return _size; }

  [[nodiscard]] bool isEmpty() const { return _size == 0; }

  [[nodiscard]] uint32_t hash(const K* key) const {
    if (key == nullptr) {
      return 0;
    }
    const uint32_t h = key->customHash();
    return h ^ (h >> 16U);
  }

  [[nodiscard]] bool containsKey(const K* key) const {
    uint32_t h = hash(key);
    Entry* e = getBucket(h);

    while (e != nullptr) {
      if (e->match(h, key)) {
        return true;
      }
      e = e->_next;
    }
    return false;
  }

  V* at(const K* key) const {
    const uint32_t h = this->hash(key);
    Entry* e = getBucket(h);

    while (e != nullptr) {
      if (e->match(h, key)) {
        return &e->_value;
      }
      e = e->_next;
    }
    // Return a default-constructed V if the key is not found
    return nullptr;
  }

  void atPut(const K* const key, const V& value) {
    const uint32_t h = hash(key);
    const uint32_t i = getBucketIdx(h);

    Entry* current = _buckets[i];

    if (current == nullptr) {
      _buckets[i] = newEntry(key, value, h);
      _size += 1;
    } else {
      insertBucketEntry(key, value, h, current);
    }

    if (_size > _capacity) {
      resize();
    }
  }

  void removeAll() {
    for (uint32_t i = 0; i < _capacity; i += 1) {
      Entry* current = _buckets[i];
      while (current != nullptr) {
        Entry* toBeDeleted = current;
        current = current->_next;
        delete toBeDeleted;
      }

      _buckets[i] = nullptr;  // Reset each element to a default value
    }
    _size = 0;
  }

  [[nodiscard]] Vector<const K*>* getKeys() {
    auto* keys = new Vector<const K*>();
    for (uint32_t i = 0; i < _capacity; i += 1) {
      Entry* current = _buckets[i];
      while (current != nullptr) {
        keys->append(current->_key);
        current = current->_next;
      }
    }
    return keys;
  }

  [[nodiscard]] Vector<V>* getValues() {
    auto* values = new Vector<V>(_size);

    for (uint32_t i = 0; i < _capacity; i += 1) {
      Entry* current = _buckets[i];
      while (current != nullptr) {
        values->append(current->_value);
        current = current->_next;
      }
    }
    return values;
  }

  void destroyValues() {
    for (uint32_t i = 0; i < _capacity; i += 1) {
      Entry* current = _buckets[i];
      while (current != nullptr) {
        delete current->_value;
        current = current->_next;
      }
    }
  }

  virtual Entry* newEntry(const K* key, V value, uint32_t hash) {
    return new Entry(hash, key, value, nullptr);
  }

 private:
  [[nodiscard]] uint32_t getBucketIdx(uint32_t hash) const {
    return (_capacity - 1) & hash;
  }

  [[nodiscard]] Entry* getBucket(uint32_t hash) const {
    return _buckets[getBucketIdx(hash)];
  }

  void insertBucketEntry(const K* key,
                         const V& value,
                         uint32_t hash,
                         Entry* head) {
    Entry* current = head;

    while (true) {
      if (current->match(hash, key)) {
        current->_value = value;
        return;
      }
      if (current->_next == nullptr) {
        _size += 1;
        current->_next = newEntry(key, value, hash);
        return;
      }
      current = current->_next;
    }
  }

  void resize() {
    Entry** oldStorage = _buckets;
    const uint32_t oldCapacity = _capacity;
    _capacity *= 2;
    auto* newStorage = new Entry* [_capacity] {};
    _buckets = newStorage;
    transferEntries(oldStorage, oldCapacity);
    delete[] oldStorage;
  }

  void transferEntries(Entry** oldStorage, uint32_t oldCapacity) {
    for (uint32_t i = 0; i < oldCapacity; i += 1) {
      Entry* current = oldStorage[i];
      if (current != nullptr) {
        oldStorage[i] = nullptr;
        if (current->_next == nullptr) {
          _buckets[current->_hash & (oldCapacity - 1)] = current;
        } else {
          splitBucket(oldCapacity, i, current);
        }
      }
    }
  }

  void splitBucket(uint32_t oldCapacity, uint32_t idx, Entry* head) {
    Entry* loHead = nullptr;
    Entry* loTail = nullptr;
    Entry* hiHead = nullptr;
    Entry* hiTail = nullptr;

    Entry* current = head;

    while (current != nullptr) {
      if ((current->_hash & oldCapacity) == 0) {
        if (loTail == nullptr) {
          loHead = current;
        } else {
          loTail->_next = current;
        }
        loTail = current;
      } else {
        if (hiTail == nullptr) {
          hiHead = current;
        } else {
          hiTail->_next = current;
        }
        hiTail = current;
      }
      current = current->_next;
    }

    if (loTail != nullptr) {
      loTail->_next = nullptr;
      _buckets[idx] = loHead;
    }
    if (hiTail != nullptr) {
      hiTail->_next = nullptr;
      _buckets[idx + oldCapacity] = hiHead;
    }
  }
};
