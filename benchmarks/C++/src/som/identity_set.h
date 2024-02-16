#pragma once

#include "set.h"

#include <cstdint>

template <typename E>
class IdentitySet : public Set<E> {
 public:
  explicit IdentitySet() = default;
  explicit IdentitySet(size_t size) : Set<E>(size) {}

  bool contains(E& obj) {
    return hasSome([&obj](const E& e) { return e == obj; });
  }
};
