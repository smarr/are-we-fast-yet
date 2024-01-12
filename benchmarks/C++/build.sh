#!/bin/bash
if [ -z "$OPT" ]; then
  OPT='-O3 -flto'
fi

# start by trying to find a suitable clang
CMD_VERSION='-mp-17'

if ! [ -x "$(command -v clang++$CMD_VERSION)" ]; then
  CMD_VERSION='-17'
  if ! [ -x "$(command -v clang++$CMD_VERSION)" ]; then
    CMD_VERSION='-15'
  fi
fi

CMD="clang++$CMD_VERSION"

if [ -z "$CXX" ]; then
  CXX="$CMD"
fi

SRC='src/harness.cpp src/deltablue.cpp src/memory/object_tracker.cpp src/richards.cpp'

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
  $CXX -Wall -Wextra -Wno-unused-private-field $SANATIZE $OPT -ffp-contract=off -std=c++17 $SRC -o harness-$CXX

  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX NBody      10 250000
  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX Richards   10 100
  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX DeltaBlue  10 1200
  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX Mandelbrot 10 500
  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX Queens     10 1000
  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX Towers     10 600
  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX Bounce     10 1500
  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX CD         10 250
  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX Json       10 100
  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX List       10 1500
  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX Storage    10 1000
  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX Sieve      10 3000
  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX Mandelbrot 10 500
  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX Permute    10 1000
  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX Bounce     10 1500
  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX Mandelbrot 10 500
  LLVM_PROFILE_FILE="prof-%p.profraw" ./harness-$CXX Havlak     10 1500

  llvm-profdata$CMD_VERSION merge -output=prof.profdata prof-*.profraw

  OPT="$ORG_OPT -fprofile-use=prof.profdata"
  $CXX -Wall -Wextra -Wno-unused-private-field -march=native $SANATIZE $OPT -ffp-contract=off -std=c++17 $SRC -o harness-$CXX
  EXIT_CODE=$?
  rm *.profraw prof.profdata
  exit $EXIT_CODE
else
  echo Bulding with $OPT optimizations
  SANATIZE=''
fi

exec $CXX -Wall -Wextra -Wno-unused-private-field -march=native $SANATIZE $OPT -ffp-contract=off -std=c++17 $SRC -o harness-$CXX
