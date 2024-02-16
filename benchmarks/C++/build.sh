#!/bin/bash
SCRIPT_PATH="$(dirname "$0")"
source "$SCRIPT_PATH/../script.inc"

if [ -z "$OPT" ]; then
  OPT='-O3 -flto -march=native'
fi
NAME_OPT="${OPT//[[:blank:]]/}"
NAME_OPT="${NAME_OPT//flto/lto}"

# start by trying to find a suitable clang
CMD_VERSION='-mp-17'

if ! [ -x "$(command -v clang++$CMD_VERSION)" ]; then
  CMD_VERSION='-17'
  if ! [ -x "$(command -v clang++$CMD_VERSION)" ]; then
    CMD_VERSION='-15'
  fi
fi

if [ -z "$CXX" ]; then
  CMD="clang++$CMD_VERSION"
  CXX="$CMD"

  # trying to avoid bugs, CXX should be used at this point
  unset CMD
fi

if [[ $CXX == *"clang"* ]]; then
  COMPILER_NAME="clang"
  WARNINGS="-Wall -Wextra -Wno-unused-private-field"
else
  # we assume gcc
  COMPILER_NAME="gcc"
  WARNINGS="-Wall -Wextra"
fi
COMP_OPT="-ffp-contract=off -std=c++17"

echo "Using compiler type: $COMPILER_NAME"

SRC='src/harness.cpp src/deltablue.cpp src/memory/object_tracker.cpp src/richards.cpp'

BENCHMARKS=("NBody      10 250000"
            "Richards   10 100"
            "DeltaBlue  10 1200"
            "Mandelbrot 10 500"
            "Queens     10 1000"
            "Towers     10 600"
            "Bounce     10 1500"
            "CD         10 250"
            "Json       10 100"
            "List       10 1500"
            "Storage    10 1000"
            "Sieve      10 3000"
            "Mandelbrot 10 500"
            "Permute    10 1000"
            "Bounce     10 1500"
            "Mandelbrot 10 500"
            "Havlak     10 1500")

pushd "$SCRIPT_PATH"

if [ "$1" = "style" ]
then
    INFO Check Format
    ./build.sh check-format
    FMT_EXIT=$?

    INFO Run Lint
    ./build.sh lint
    LNT_EXIT=$?
    exit $((FMT_EXIT + LNT_EXIT))
fi

if [ "$1" = "format" ]
then
  CMD=clang-format$CMD_VERSION
  type -P "$CMD" || CMD=clang-format
  exec $CMD -i --style=file src/*.cpp src/*.h src/**/*.cpp src/**/*.h
fi

if [ "$1" = "check-format" ]
then
  CMD=clang-format$CMD_VERSION
  type -P "$CMD" || CMD=clang-format
  exec $CMD --style=file --dry-run --Werror src/*.cpp src/*.h  src/**/*.cpp src/**/*.h
fi

if [ "$1" = "lint" ]
then
  CMD=clang-tidy$CMD_VERSION
  type -P "$CMD" || CMD=clang-tidy
  exec $CMD --config-file=.clang-tidy -header-filter=.* src/*.cpp
fi

if [ "$1" = "leak-san" ]
then
  echo Bulding with Leak Sanitizer enabled
  # then run with LSAN_OPTIONS=suppressions=.clang-leak.txt ./harness Towers 20 1000
  SANATIZE='-g -fsanitize=leak'
  OPT='-Og'
elif [ "$1" = "address-san" ]
then
  echo Bulding with Address Sanitizer enabled
  SANATIZE='-g -fsanitize=address'
  OPT='-Og'
elif [ "$1" = "memory-san" ]
then
  echo Bulding with Memory Sanitizer enabled
  SANATIZE='-g -fsanitize=memory'
  OPT='-Og'
elif [ "$1" = "undefined-san" ]
then
  echo Bulding with Undefined Behavior Sanitizer enabled
  SANATIZE='-g -fsanitize=undefined'
  OPT='-Og'
elif [ "$1" = "pedantic" ]
then
  DISABLED_WARNINGS='-Wno-poison-system-directories -Wno-c++98-compat
                     -Wno-shadow
                     -Wno-shadow-field-in-constructor -Wno-unused-private-field
                     -Wno-padded
                     -Wno-global-constructors
                     -Wno-exit-time-destructors
                     -Wno-float-equal
                     -Wno-sign-conversion
                     -Wno-unsafe-buffer-usage -Wno-weak-vtables'
  SANATIZE="-Weverything -pedantic -Wall -Wextra $DISABLED_WARNINGS"
  echo Bulding with pedantic warnings and $OPT optimizations
elif [ "$1" = "pgo" ]
then
  echo Bulding with PGO optimizations
  ORG_OPT="$OPT"

  OPT="$ORG_OPT -fprofile-generate"
  $CXX $WARNINGS $SANATIZE $OPT $COMP_OPT $SRC -o harness-$CXX

  for b in "${BENCHMARKS[@]}"; do
    LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX $b
  done

  if [ "$COMPILER_NAME" = "clang" ]; then
    llvm-profdata$CMD_VERSION merge -output=prof.profdata prof-*.profraw
    OPT="$ORG_OPT -fprofile-use=prof.profdata"
  else
    OPT="$ORG_OPT -fprofile-use"
  fi

  $CXX $WARNINGS $SANATIZE $OPT $COMP_OPT $SRC -o harness-$CXX$NAME_OPT-pgo
  EXIT_CODE=$?
  rm -f *.profraw prof.profdata *.gcda
  exit $EXIT_CODE
else
  echo Bulding with $OPT optimizations
  SANATIZE=''
fi

echo Binary name: harness-$CXX$NAME_OPT
eval $CXX $WARNINGS $SANATIZE $OPT $COMP_OPT $SRC -o harness-$CXX$NAME_OPT
ln -sf harness-$CXX$NAME_OPT harness
