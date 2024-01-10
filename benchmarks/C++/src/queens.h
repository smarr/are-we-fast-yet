#pragma once

#include <algorithm>
#include <any>

#include "benchmark.h"

class Queens : public Benchmark {
 private:
  bool* free_maxs{nullptr};
  bool* free_rows{nullptr};
  bool* free_mins{nullptr};
  int32_t* queen_rows{nullptr};

  bool queens() {
    free_rows = new bool[8];
    std::fill_n(free_rows, 8, true);
    free_maxs = new bool[16];
    std::fill_n(free_maxs, 16, true);
    free_mins = new bool[16];
    std::fill_n(free_mins, 16, true);
    queen_rows = new int32_t[8];
    std::fill_n(queen_rows, 8, -1);

    const bool result = place_queen(0);

    delete[] free_rows;
    delete[] free_maxs;
    delete[] free_mins;
    delete[] queen_rows;

    return result;
  }

  bool place_queen(int32_t c) {
    for (int32_t r = 0; r < 8; r += 1) {
      if (get_row_column(r, c)) {
        queen_rows[r] = c;
        set_row_column(r, c, false);

        if (c == 7) {
          return true;
        }

        if (place_queen(c + 1)) {
          return true;
        }
        set_row_column(r, c, true);
      }
    }
    return false;
  }

  bool get_row_column(int32_t r, int32_t c) {
    return free_rows[r] && free_maxs[c + r] && free_mins[c - r + 7];
  }

  void set_row_column(int32_t r, int32_t c, bool v) {
    free_rows[r] = v;
    free_maxs[c + r] = v;
    free_mins[c - r + 7] = v;
  }

 public:
  std::any benchmark() override {
    bool result = true;
    for (int32_t i = 0; i < 10; i += 1) {
      result = result && queens();
    }
    return result;
  }

  bool verify_result(std::any result) override {
    return std::any_cast<bool>(result);
  }
};
