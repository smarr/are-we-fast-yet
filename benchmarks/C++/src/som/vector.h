#pragma once

#include <algorithm>
#include <functional>
#include <iostream>
#include <memory>
#include <stdexcept>

constexpr const size_t INITIAL_SIZE = 10;

template <typename E>
class Vector {
 private:
  E* storage;
  size_t _capacity;
  size_t _firstIdx{0};
  size_t _lastIdx{0};

  void sort(size_t i, size_t j, std::function<int(const E&, const E&)> c) {
    if (c == nullptr) {
      defaultSort(i, j);
      return;  // Make sure to return if c is nullptr.
    }

    const size_t n = j + 1 - i;
    if (n <= 1) {
      return;
    }

    E di = storage[i];
    E dj = storage[j];

    if (c(di, dj) > 0) {
      swap(storage, i, j);
      E tt = di;
      di = dj;
      dj = tt;
    }

    if (n > 2) {
      const size_t ij = (i + j) / 2;
      E dij = storage[ij];

      if (c(di, dij) <= 0) {
        if (c(dij, dj) > 0) {
          swap(storage, j, ij);
          dij = dj;
        }
      } else {
        swap(storage, i, ij);
        dij = di;
      }

      if (n > 3) {
        size_t k = i;
        size_t l = j - 1;

        while (true) {
          while (k <= l && c(dij, storage[l]) <= 0) {
            l -= 1;
          }

          k += 1;
          while (k <= l && c(storage[k], dij) <= 0) {
            k += 1;
          }

          if (k > l) {
            break;
          }
          swap(storage, k, l);
        }

        sort(i, l, c);
        sort(k, j, c);
      }
    }
  }

 public:
  static Vector<E>* with(const E& elem) {
    auto v = new Vector<E>(1);
    v->append(elem);
    return v;
  }

  explicit Vector(size_t size)
      : storage(size == 0 ? nullptr : new E[size]), _capacity(size) {}

  explicit Vector() : Vector(0) {}  // NOLINT

  Vector(const Vector& other) = default;

  ~Vector() { delete[] storage; }

  void destroyValues() {
    for (size_t i = _firstIdx; i < _lastIdx; i += 1) {
      delete storage[i];
    }
  }

  [[nodiscard]] E* at(size_t idx) const {
    if (storage == nullptr || idx >= _lastIdx - _firstIdx) {
      return nullptr;
    }
    return &storage[_firstIdx + idx];
  }

  void atPut(size_t idx, const E& val) {
    if (storage == nullptr) {
      _capacity = std::max(idx + 1, INITIAL_SIZE);
      storage = new E[_capacity];
    } else if (idx >= _lastIdx - _firstIdx) {
      size_t newLength = _capacity;
      while (newLength <= idx + _firstIdx) {
        newLength *= 2;
      }
      E* newStorage = new E[newLength];
      for (size_t i = 0; i < _lastIdx; i += 1) {
        newStorage[i] = storage[i];
      }
      delete[] storage;
      storage = newStorage;
      _capacity = newLength;
    }
    storage[_firstIdx + idx] = val;
    if (_lastIdx < idx + 1) {
      _lastIdx = idx + 1;
    }
  }

  void append(const E& elem) {
    if (storage == nullptr) {
      storage = new E[INITIAL_SIZE];
      _capacity = INITIAL_SIZE;
    } else if (_lastIdx >= _capacity) {
      // Need to expand _capacity first
      _capacity *= 2;
      E* newStorage = new E[_capacity];
      for (size_t i = 0; i < _lastIdx; i += 1) {
        newStorage[i] = storage[i];
      }
      delete[] storage;
      storage = newStorage;
    }

    storage[_lastIdx] = elem;
    _lastIdx += 1;
  }

  [[nodiscard]] bool isEmpty() const { return _lastIdx == _firstIdx; }

  void forEach(const std::function<void(const E&)>& fn) const {
    for (size_t i = _firstIdx; i < _lastIdx; i += 1) {
      fn(storage[i]);
    }
  }

  bool hasSome(const std::function<bool(const E&)>& fn) const {
    for (size_t i = _firstIdx; i < _lastIdx; i += 1) {
      if (fn(storage[i])) {
        return true;
      }
    }
    return false;
  }

  E getOne(const std::function<bool(const E&)>& fn) const {
    for (size_t i = _firstIdx; i < _lastIdx; i += 1) {
      const E& e = storage[i];
      if (fn(e)) {
        return e;
      }
    }
    return nullptr;  // Return a default-constructed object if not found
  }

  [[nodiscard]] E first() const {
    if (isEmpty()) {
      return nullptr;  // Return a default-constructed object for an empty
                       // vector
    }
    return storage[_firstIdx];
  }

  E removeFirst() {
    if (isEmpty()) {
      return nullptr;  // Return a default-constructed object for an empty
                       // vector
    }
    _firstIdx += 1;
    return storage[_firstIdx - 1];
  }

  bool remove(const E& obj) {
    if (storage == nullptr || isEmpty()) {
      return false;
    }

    bool found = false;
    size_t newLast = _firstIdx;
    for (size_t i = _firstIdx; i < _lastIdx; i += 1) {
      if (storage[i] == obj) {
        found = true;
      } else {
        storage[newLast] = storage[i];
        newLast += 1;
      }
    }
    _lastIdx = newLast;
    return found;
  }

  void removeAll() {
    destroyValues();

    _firstIdx = 0;
    _lastIdx = 0;

    if (storage != nullptr) {
      delete[] storage;
      storage = new E[_capacity];
    }
  }

  [[nodiscard]] size_t size() const { return _lastIdx - _firstIdx; }

  [[nodiscard]] size_t getCapacity() const {
    return storage == nullptr ? 0 : _capacity;
  }

  void sort(std::function<int(const E&, const E&)> c) {
    if (size() > 0) {
      sort(_firstIdx, _lastIdx - 1, c);
    }
  }

 private:
  static void swap(E*, size_t, size_t) noexcept {
    std::cerr << "swap not implemented\n";
    exit(1);
  }

  void defaultSort(size_t, size_t) {
    std::cerr << "defaultSort not implemented\n";
    exit(1);
  }
};
