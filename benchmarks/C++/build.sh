#!/bin/bash
if [ "$1" = "format" ]
then
  CMD=clang-format
  type -P "$CMD" || CMD=clang-format-mp-16
  $CMD -i --style=file src/*.cpp src/*.h src/**/*.cpp src/**/*.h
  exit 0
fi

if [ "$1" = "check-format" ]
then
  CMD=clang-format
  type -P "$CMD" || CMD=clang-format-mp-16
  $CMD --style=file --dry-run --Werror src/*.cpp src/*.h  src/**/*.cpp src/**/*.h
  exit 0
fi

if [ "$1" = "lint" ]
then
  CMD=clang-tidy
  type -P "$CMD" || CMD=clang-tidy-mp-16
  $CMD --config-file=.clang-tidy -header-filter=.* src/*.cpp
  exit 0
fi

if [ "$1" = "sanitize" ]
then
  SANATIZE='-g -fsanitize=leak'
  OPT='-Og'
else
  SANATIZE=''
  OPT='-O3'
fi

clang++-mp-16 -Wall -Wextra $SANATIZE $OPT -ffp-contract=off -std=c++17 src/harness.cpp src/deltablue.cpp -o harness
