#pragma once

#include <any>

#include "benchmark.h"

class Element {
 private:
  std::any val;
  Element* next{nullptr};

 public:
  explicit Element(std::any v) : val(std::move(v)) {}

  ~Element() {
    if (next != nullptr) {
      delete next;
    }
  }

  [[nodiscard]] int32_t length() {
    if (next == nullptr) {
      return 1;
    }
    return 1 + next->length();
  }

  [[nodiscard]] std::any getVal() const { return val; }
  void setVal(std::any v) { val = std::move(v); }
  [[nodiscard]] Element* getNext() const { return next; }
  void setNext(Element* e) { next = e; }
};

class List : public Benchmark {
 public:
  std::any benchmark() override {
    Element* x = makeList(15);
    Element* y = makeList(10);
    Element* z = makeList(6);

    Element* result = tail(x, y, z);
    const int32_t l = result->length();

    delete x;
    delete y;
    delete z;
    return l;
  }

 private:
  [[nodiscard]] Element* makeList(int32_t length) const {
    if (length == 0) {
      return nullptr;
    }
    auto* e = new Element(length);
    e->setNext(makeList(length - 1));
    return e;
  }

  [[nodiscard]] bool isShorterThan(Element* x, Element* y) const {
    Element* xTail = x;
    Element* yTail = y;

    while (yTail != nullptr) {
      if (xTail == nullptr) {
        return true;
      }
      xTail = xTail->getNext();
      yTail = yTail->getNext();
    }
    return false;
  }

  Element* tail(Element* x, Element* y, Element* z) {
    if (isShorterThan(y, x)) {
      return tail(tail(x->getNext(), y, z), tail(y->getNext(), z, x),
                  tail(z->getNext(), x, y));
    }
    return z;
  }

  bool verify_result(std::any result) override {
    return 10 == std::any_cast<int32_t>(result);
  }
};
