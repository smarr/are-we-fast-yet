#ifndef SOM_DICTIONARY_H
#define SOM_DICTIONARY_H

/* This code is derived from the SOM benchmarks, see AUTHORS.md file.
 *
 * Copyright (c) 2023 Rochus Keller <me@rochus-keller.ch> (for C++ migration)
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

#include <som/Vector.h>

namespace som {

template<class K, class V, class H>
class Dictionary {

protected:
    enum { INITIAL_CAPACITY = 16 };

    template<class KK, class VV>
    struct Entry;

    template<class KK, class VV>
    class EntryRef
    {
        Entry<KK, VV>* entry;
    public:
        EntryRef(Entry<KK, VV>* e = 0):entry(e)
        {
            if( e )
                e->refCount++;
        }
        EntryRef( const EntryRef<KK, VV>& rhs)
        {
            *this = rhs;
        }
        ~EntryRef()
        {
            if( entry )
            {
                entry->refCount--;
                if( entry->refCount == 0 )
                    delete entry;
            }
        }
        EntryRef& operator=(const EntryRef& rhs )
        {
            *this = rhs.ptr();
            return *this;
        }

        EntryRef& operator=(Entry<KK, VV>* rhs )
        {
            if( entry == rhs )
                return *this;
            if( entry )
            {
                entry->refCount--;
                if( entry->refCount == 0 )
                    delete entry;
            }
            entry = rhs;
            if( entry )
                entry->refCount++;
            return *this;
        }
        Entry<KK, VV>* ptr() const { return entry; }
        Entry<KK, VV>* operator->() const { return entry; }
    };

    template<class KK, class VV>
    struct Entry {

        int refCount;
        int hash;
        KK key;
        VV value;
        EntryRef<KK,VV> next;

        Entry(int hash, const KK& key, const VV& value, Entry<KK, VV>* next) {
            this->refCount = 0;
            this->hash  = hash;
            this->key   = key;
            this->value = value;
            this->next  = next;
        }

        bool match(int hash, const KK& key) {
            return this->hash == hash && key == this->key;
        }
    };

    Entry<K, V>* getBucket(int hash) {
        return buckets[getBucketIdx(hash)].ptr();
    }


private:
    EntryRef<K,V>* buckets;
    int length;
    int          sz;

public:

    Dictionary(int len):sz(0) {
        buckets = new EntryRef<K,V>[len];
        length = len;
    }

    Dictionary():sz(0),length(0) {
        length = INITIAL_CAPACITY;
        buckets = new EntryRef<K,V>[length];
    }

    ~Dictionary()
    {
        delete[] buckets;
    }

    int hash(const K& key) {
        H hash;
        const int h = hash(key);
        return h ^ ( h >> 16 );
    }

    int size() {
        return size;
    }

    bool isEmpty() {
        return size == 0;
    }

    int getBucketIdx(int hash) {
        return (length - 1) & hash;
    }

    V* at(const K& key) {
        const int h = hash(key);
        Entry<K, V>* e = getBucket(h);

        while (e != 0) {
            if (e->match(h, key)) {
                return &e->value;
            }
            e = e->next.ptr();
        }
        return 0;
    }

    bool containsKey(const K& key) {
        const int h = hash(key);
        Entry<K, V>* e = getBucket(h);

        while (e != 0) {
            if (e->match(h, key)) {
                return true;
            }
            e = e->next.ptr();
        }
        return false;
    }

    void atPut(const K& key, const V& value) {
        const int h = hash(key);
        const int i = getBucketIdx(h);

        Entry<K, V>* current = buckets[i].ptr();

        if (current == 0) {
            buckets[i] = newEntry(key, value, h);
            sz += 1;
        } else {
            insertBucketEntry(key, value, h, current);
        }

        if (sz > length) {
            resize();
        }
    }

    void removeAll() {
        for( int i = 0; i < length; i++ )
            buckets[i] = 0;
        sz = 0;
    }

    void getKeys(Vector<K>& keys) {
        keys.removeAll();
        keys.expand(sz);
        for (int i = 0; i < length; ++i) {
            Entry<K, V>* current = buckets[i].ptr();
            while (current != 0) {
                keys.append(current->key);
                current = current->next.ptr();
            }
        }
    }

    void getValues(Vector<V>& values) {
        values.removeAll();
        values.expand(sz);
        for (int i = 0; i < length; ++i) {
            Entry<K, V>* current = buckets[i].ptr();
            while (current != 0) {
                values.append(current->value);
                current = current->next.ptr();
            }
        }
    }

    void resize() {
        EntryRef<K,V>* oldStorage = buckets;
        const int oldLen = length;

        length *= 2;
        EntryRef<K,V>* newStorage = new EntryRef<K,V>[length];
        buckets = newStorage;
        transferEntries(oldStorage,oldLen);
        delete[] oldStorage;
    }


protected:
    Entry<K, V>* newEntry(const K& key, const V& value, int hash) {
        return new Entry<K,V>(hash, key, value, 0);
    }

    void insertBucketEntry(const K& key, const V& value, int hash, Entry<K, V>* head) {
        Entry<K, V>* current = head;

        while (true) {
            if (current->match(hash, key)) {
                current->value = value;
                return;
            }
            if (current->next.ptr() == 0) {
                sz += 1;
                current->next = newEntry(key, value, hash);
                return;
            }
            current = current->next.ptr();
        }
    }

    void transferEntries(EntryRef<K,V>* oldStorage, int oldLen) {
        for (int i = 0; i < oldLen; ++i) {
            Entry<K, V>* current = oldStorage[i].ptr();
            if (current != 0) {
                if (current->next.ptr() == 0) {
                    buckets[current->hash & (length - 1)] = current;
                } else {
                    splitBucket(oldStorage, oldLen, i, current);
                }
            }
        }
    }

    void splitBucket(EntryRef<K,V>* oldStorage, int oldLen, int i, Entry<K, V>* head) {
        EntryRef<K,V> loHead;
        EntryRef<K,V> loTail;
        EntryRef<K,V> hiHead;
        EntryRef<K,V> hiTail;
        EntryRef<K,V> current = head;

        while (current.ptr()!= 0) {
            if ((current->hash & oldLen) == 0) {
                if (loTail.ptr() == 0) {
                    loHead = current;
                } else {
                    loTail->next = current;
                }
                loTail = current;
            } else {
                if (hiTail.ptr() == 0) {
                    hiHead = current;
                } else {
                    hiTail->next = current;
                }
                hiTail = current;
            }
            current = current->next;
        }

        if (loTail.ptr() != 0) {
            loTail->next = 0;
            buckets[i] = loHead;
        }
        if (hiTail.ptr() != 0) {
            hiTail->next = 0;
            buckets[i + oldLen] = hiHead;
        }
    }

};

template<class K, class V, class H>
class IdentityDictionary : public Dictionary<K,V,H> {
public:
  IdentityDictionary(int size):Dictionary<K,V,H>(size) {}

  IdentityDictionary(){}
};

}

#endif // SOM_DICTIONARY_H
