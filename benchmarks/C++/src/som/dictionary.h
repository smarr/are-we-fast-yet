#pragma once

#include <memory>
#include "vector.h"

class CustomHash {
 public:
  CustomHash() = default;
  virtual ~CustomHash() = default;
  [[nodiscard]] virtual int32_t customHash() const = 0;
};

template <typename V>
class IdentityDictionary;

/**
 * The Dictionary class does not manage the memory of keys and values.
 *
 * The memory of Entry objects is managed by ownership through the _buckets
 * field. Thus, they are anchored in the _buckets, and only freed from there.
 */
template <typename V>
class Dictionary {
  friend class IdentityDictionary<V>;

 private:
  class Entry {
    friend class IdentityDictionary<V>;
    friend class Dictionary<V>;

   private:
    int32_t _hash;
    const CustomHash* const _key;
    V _value;
    Entry* _next;

   public:
    Entry(int32_t h, const CustomHash* k, const V& v, Entry* n)
        : _hash(h), _key(k), _value(v), _next(n) {}
    virtual ~Entry() = default;

    virtual bool match(int32_t h, const CustomHash* const k) {
      return _hash == h && _key == k;
    }

    [[nodiscard]] const CustomHash* getKey() const { return _key; }

    [[nodiscard]] int32_t getHash() const { return _hash; }
  };

  static const int32_t INITIAL_CAPACITY = 16;

  Entry** _buckets;
  int32_t _size{0};
  int32_t _capacity;

 public:
  explicit Dictionary(int32_t capacity = INITIAL_CAPACITY)
      : _buckets(new Entry* [capacity] {}), _capacity(capacity) {}

  virtual ~Dictionary() {
    removeAll();
    delete[] _buckets;
  }

  [[nodiscard]] int32_t getSize() const { return _size; }

  [[nodiscard]] bool isEmpty() const { return _size == 0; }

  [[nodiscard]] int32_t hash(const CustomHash* key) const {
    if (key == nullptr) {
      return 0;
    }
    const int32_t h = key->customHash();
    return h ^ (h >> 16);
  }

  [[nodiscard]] bool containsKey(const CustomHash* key) const {
    int32_t h = hash(key);
    Entry* e = getBucket(h);

    while (e != nullptr) {
      if (e->match(h, key)) {
        return true;
      }
      e = e->_next;
    }
    return false;
  }

  V atPtr(const CustomHash* key) const {
    const int32_t h = hash(key);
    Entry* e = getBucket(h);

    while (e != nullptr) {
      if (e->match(h, key)) {
        return e->_value;
      }
      e = e->_next;
    }
    return nullptr;
  }

  V at(const CustomHash* key) const {
    const int32_t h = this->hash(key);
    Entry* e = getBucket(h);

    while (e != nullptr) {
      if (e->match(h, key)) {
        return e->_value;
      }
      e = e->_next;
    }
    // Return a default-constructed V if the key is not found
    return V();
  }

  void atPut(const CustomHash* const key, const V& value) {
    const int32_t h = hash(key);
    const int32_t i = getBucketIdx(h);

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
    for (int32_t i = 0; i < _capacity; i++) {
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

  [[nodiscard]] Vector<const CustomHash*>* getKeys() {
    auto* keys = new Vector<const CustomHash*>();
    for (int32_t i = 0; i < _capacity; i++) {
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

    for (int32_t i = 0; i < _capacity; i++) {
      Entry* current = _buckets[i];
      while (current != nullptr) {
        values->append(current->_value);
        current = current->_next;
      }
    }
    return values;
  }

  void destroyValues() {
    for (int32_t i = 0; i < _capacity; i++) {
      Entry* current = _buckets[i];
      while (current != nullptr) {
        delete current->_value;
        current = current->_next;
      }
    }
  }

  virtual Entry* newEntry(const CustomHash* key, V value, int32_t hash) {
    return new Entry(hash, key, value, nullptr);
  }

 private:
  [[nodiscard]] int32_t getBucketIdx(int32_t hash) const {
    return (_capacity - 1) & hash;
  }

  [[nodiscard]] Entry* getBucket(int32_t hash) const {
    return _buckets[getBucketIdx(hash)];
  }

  void insertBucketEntry(const CustomHash* key,
                         const V& value,
                         int32_t hash,
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
    const int32_t oldCapacity = _capacity;
    _capacity *= 2;
    auto* newStorage = new Entry*[_capacity];
    _buckets = newStorage;
    transferEntries(oldStorage, oldCapacity);
    delete[] oldStorage;
  }

  void transferEntries(Entry** oldStorage, int32_t oldCapacity) {
    for (int32_t i = 0; i < oldCapacity; i++) {
      Entry* current = oldStorage[i];
      if (current != nullptr) {
        oldStorage[i] = nullptr;
        if (current->_next == nullptr) {
          _buckets[current->_hash & (oldCapacity - 1)] = current;
        } else {
          splitBucket(i, current, oldCapacity);
        }
      }
    }
  }

  void splitBucket(int32_t idx, Entry* head, int32_t oldCapacity) {
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