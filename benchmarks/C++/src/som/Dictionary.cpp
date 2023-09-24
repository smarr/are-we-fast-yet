#ifndef DICTIONARY
#define DICTIONARY


#include <memory>
#include "Vector.cpp"

class CustomHash {
    public:
        CustomHash() = default;
        virtual int customHash()=0;
};

template <typename V>
class Dictionary {

    public:
        class Entry {
            public:
                int _hash;
                shared_ptr<CustomHash> _key;
                V _value;
                std::shared_ptr<Entry> _next;

            public:
                Entry(int h, const shared_ptr<CustomHash>& k, const V& v, std::shared_ptr<Entry> n)
                    : _hash(h), _key(k), _value(v), _next(n) {}

                virtual bool match(int h, const shared_ptr<CustomHash>& k) {
                    return _hash == h && _key == k;
                }

                const shared_ptr<CustomHash>& getKey() const {
                    return _key;
                }

                int getHash() const {
                    return _hash;
                }
        };

        static const int INITIAL_CAPACITY = 16;

        std::shared_ptr<Entry>* _buckets;
        int _size;
        int _capacity;

    public:
        Dictionary(int capacity = INITIAL_CAPACITY) : _size(0), _capacity(capacity) {
            _buckets = new std::shared_ptr<Entry>[capacity]();
        }

        ~Dictionary() {
            removeAll();
            delete[] _buckets;
        }

        int getSize() const {
            return _size;
        }

        bool isEmpty() const {
            return _size == 0;
        }

        int hash(const shared_ptr<CustomHash>& key) const {
            if (key == nullptr) {
                return 0;
            }
            int h = key->customHash();
            return h ^ (h >> 16);
        }

        bool containsKey(const shared_ptr<CustomHash>& key) const {
            int h = hash(key);
            std::shared_ptr<Entry> e = getBucket(h);

            while (e != nullptr) {
                if (e->match(h, key)) {
                    return true;
                }
                e = e->_next;
            }
            return false;
        }

        V atPtr(const shared_ptr<CustomHash>& key) const {
            int h = hash(key);
            std::shared_ptr<Entry> e = getBucket(h);

            while (e != nullptr) {
                if (e->match(h, key)) {
                    return e->_value;
                }
                e = e->_next;
            }
            return nullptr;
        }


        V at(const shared_ptr<CustomHash>& key) const {
            int h = this->hash(key);
            std::shared_ptr<Entry> e = getBucket(h);

            while (e != nullptr) {
                if (e->match(h, key)) {
                    return e->_value;
                }
                e = e->_next;
            }
            // Return a default-constructed V if the key is not found
            return V();
        }

        void atPut(const shared_ptr<CustomHash>& key, const V& value) {
            int h = hash(key);
            int i = getBucketIdx(h);

            std::shared_ptr<Entry> current = _buckets[i];

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
            for(int i = 0; i < _capacity; i++) {
                _buckets[i] = nullptr; // Reset each element to a default value
            }
            _size = 0;
        }

        shared_ptr<Vector<shared_ptr<CustomHash>>> getKeys() {
            shared_ptr<Vector<shared_ptr<CustomHash>>> keys = make_shared<Vector<shared_ptr<CustomHash>>>();
            for (int i = 0; i < _capacity; i++) {
                std::shared_ptr<Entry> current = _buckets[i];
                while (current != nullptr) {
                    keys->append(current->_key);
                    current = current->_next;
                }
            }
            return keys;
        }

        shared_ptr<Vector<V>> getValues() {
            shared_ptr<Vector<V>> values = make_shared<Vector<V>>(_size);

            for (int i = 0; i < _capacity; i++) {
                std::shared_ptr<Entry> current = _buckets[i];
                while (current != nullptr) {
                    values->append(current->_value);
                    current = current->_next;
                }
            }
            return values;
        }

        virtual std::shared_ptr<Entry> newEntry(shared_ptr<CustomHash> key, V value, int hash) {
            return make_shared<Entry>(hash, key, value, nullptr);
        }

    private:
        int getBucketIdx(int hash) const {
            return (_capacity - 1) & hash;
        }

        std::shared_ptr<Entry> getBucket(int hash) const {
            return _buckets[getBucketIdx(hash)];
        }

        void insertBucketEntry(const shared_ptr<CustomHash>& key, const V& value, int hash, std::shared_ptr<Entry> head) {
            std::shared_ptr<Entry> current = head;

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
            std::shared_ptr<Entry>* oldStorage = _buckets;
            int oldCapacity = _capacity;
            _capacity *= 2;
            std::shared_ptr<Entry>* newStorage = new shared_ptr<Entry>[_capacity]();
            _buckets = newStorage;
            transferEntries(oldStorage, oldCapacity);
            delete[] oldStorage;
        }

        void transferEntries(std::shared_ptr<Entry>* oldStorage, int oldCapacity) {
            for (int i = 0; i < oldCapacity; i++) {
                shared_ptr<Entry> current = oldStorage[i];
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

        void splitBucket(int idx, std::shared_ptr<Entry> head, int oldCapacity) {
            std::shared_ptr<Entry> loHead = nullptr;
            std::shared_ptr<Entry> loTail = nullptr;
            std::shared_ptr<Entry> hiHead = nullptr;
            std::shared_ptr<Entry> hiTail = nullptr;

            std::shared_ptr<Entry> current = head;

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


#endif //DICTIONARY