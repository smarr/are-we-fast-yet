#ifndef IDENTITYDICTIONARY
#define IDENTITYDICTIONARY

#include "Dictionary.cpp"
#include <memory>

template <typename V>
class IdentityDictionary : public Dictionary<V> {
    public:
        class IdEntry : public Dictionary<V>::Entry {
            public:
                IdEntry(int hash, const shared_ptr<CustomHash>& key, const V& value, std::shared_ptr<typename Dictionary<V>::Entry> next)
                    : Dictionary<V>::Entry(hash, key, value, next) {}

                bool match(int h, const shared_ptr<CustomHash>& k) override {
                    return this->getHash() == h && this->getKey() == k;
                }
        };

    public:
        IdentityDictionary(const int size) : Dictionary<V>(size) {}
        IdentityDictionary() : Dictionary<V>(Dictionary<V>::INITIAL_CAPACITY) {}

        std::shared_ptr<typename Dictionary<V>::Entry> newEntry(shared_ptr<CustomHash> key, V value, int hash) override {
            return make_shared<IdEntry>(hash, key, value, nullptr);
        }
};

#endif //IDENTITYDICTIONARY