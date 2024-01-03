#pragma once

class Random {
 private:
  int32_t seed{74755};

 public:
  Random() = default;

  int32_t next() {
    seed = ((seed * 1309) + 13849) & 65535;
    return seed;
  }
};
