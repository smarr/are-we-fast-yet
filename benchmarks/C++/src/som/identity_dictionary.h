#pragma once

#include <memory>
#include "dictionary.h"

template <typename K, typename V>
class IdentityDictionary : public Dictionary<K, V> {
 private:
  class IdEntry : public Dictionary<K, V>::Entry {
   public:
    IdEntry(uint32_t hash,
            const K* key,
            const V& value,
            typename Dictionary<K, V>::Entry* next)
        : Dictionary<K, V>::Entry(hash, key, value, next) {}
    ~IdEntry() override = default;

    bool match(uint32_t h, const K* k) override {
      return this->getHash() == h && this->getKey() == k;
    }
  };

 public:
  explicit IdentityDictionary(const uint32_t size) : Dictionary<K, V>(size) {}
  IdentityDictionary() : Dictionary<K, V>(Dictionary<K, V>::INITIAL_CAPACITY) {}

  typename Dictionary<K, V>::Entry* newEntry(const K* key,
                                             V value,
                                             uint32_t hash) override {
    return new IdEntry(hash, key, value, nullptr);
  }
};
