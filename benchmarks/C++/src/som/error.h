
#pragma once

#include <string>

class Error : virtual public std::exception {
 private:
  std::string _what;

 public:
  explicit Error(std::string const& what) : _what(what) {}  // NOLINT

  explicit Error(const char* what) : _what(std::string(what)) {}

  [[nodiscard]] const char* what() const noexcept override {
    return (_what.c_str());
  }
};
