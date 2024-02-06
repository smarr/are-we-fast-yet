#pragma once

#include <cstdint>

#include "vector.h"

template <typename E>
class Set {
 private:
  Vector<E> items{INITIAL_SIZE};

 public:
  explicit Set() = default;
  explicit Set(size_t size) : items(size) {}

  [[nodiscard]] size_t size() const { return items.size(); }

  void forEach(const std::function<void(const E&)>& fn) const {
    items.forEach(fn);
  }

  [[nodiscard]] bool hasSome(const std::function<bool(const E&)>& fn) const {
    return items.hasSome(fn);
  }

  [[nodiscard]] E getOne(const std::function<bool(const E&)>& fn) const {
    return items.getOne(fn);
  }

  void add(E obj) {
    if (!contains(obj)) {
      items.append(obj);
    }
  }

  template <typename T>
  [[nodiscard]] Vector<T>* collect(const std::function<T(const E&)>& fn) const {
    auto* coll = new Vector<T>();
    forEach([&coll, &fn](const E& e) { coll->append(fn(e)); });
    return coll;
  }

  [[nodiscard]] bool contains(E& obj) {
    return hasSome([&obj](const E& e) {
      // C++17 compile-time magic to avoid issues with non-pointer types
      if constexpr (std::is_same_v<int32_t, E>) {
        return e == obj;
      } else {
        return e->equal(obj);
      }
    });
  }

  void removeAll() { items.removeAll(); }
};
