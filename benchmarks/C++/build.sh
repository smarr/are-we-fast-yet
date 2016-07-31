#!/bin/bash
if [ "$1" = "format" ]
then
  CMD=clang-format
  type -P "$CMD" || CMD=clang-format-mp-16
  $CMD -i --style=file src/*.cpp src/*.h
  exit 0
fi

if [ "$1" = "check-format" ]
then
  CMD=clang-format
  type -P "$CMD" || CMD=clang-format-mp-16
  $CMD --style=file --dry-run --Werror src/*.cpp src/*.h
  exit 0
fi

if [ "$1" = "lint" ]
then
  CMD=clang-tidy
  type -P "$CMD" || CMD=clang-tidy-mp-16
  $CMD --config-file=.clang-tidy -header-filter=.* src/*.cpp
  exit 0
fi

clang++-mp-16 -Wall -Wextra  -O2 -ffp-contract=off -std=c++17 src/harness.cpp -o harness
