#pragma once

#include <memory>
#include "dictionary.h"

template <typename V>
class IdentityDictionary : public Dictionary<V> {
 private:
  class IdEntry : public Dictionary<V>::Entry {
   public:
    IdEntry(int hash,
            const CustomHash* key,
            const V& value,
            typename Dictionary<V>::Entry* next)
        : Dictionary<V>::Entry(hash, key, value, next) {}
    ~IdEntry() override = default;

    bool match(int h, const CustomHash* k) override {
      return this->getHash() == h && this->getKey() == k;
    }
  };

 public:
  explicit IdentityDictionary(const int size) : Dictionary<V>(size) {}
  IdentityDictionary() : Dictionary<V>(Dictionary<V>::INITIAL_CAPACITY) {}

  typename Dictionary<V>::Entry* newEntry(const CustomHash* key,
                                          V value,
                                          int32_t hash) override {
    return new IdEntry(hash, key, value, nullptr);
  }
};
