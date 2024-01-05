#!/bin/bash

# start by trying to find a suitable clang
CMD_VERSION='-mp-17'

if ! [ -x "$(command -v clang++$CMD_VERSION)" ]; then
  CMD_VERSION='-17'
fi

CMD="clang++$CMD_VERSION"

if [ "$1" = "format" ]
then
  CMD=clang-format
  type -P "$CMD" || CMD=clang-format$CMD_VERSION
  exec $CMD -i --style=file src/*.cpp src/*.h src/**/*.cpp src/**/*.h
fi

if [ "$1" = "check-format" ]
then
  CMD=clang-format
  type -P "$CMD" || CMD=clang-format$CMD_VERSION
  exec $CMD --style=file --dry-run --Werror src/*.cpp src/*.h  src/**/*.cpp src/**/*.h
fi

if [ "$1" = "lint" ]
then
  CMD=clang-tidy
  type -P "$CMD" || CMD=clang-tidy$CMD_VERSION
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
  OPT='-O3'
  echo Bulding with pedantic warnings and $OPT optimizations
else
  
  echo Bulding with $OPT optimizations
  SANATIZE=''
fi

SRC='src/harness.cpp src/deltablue.cpp src/memory/object_tracker.cpp'

exec $CMD -Wall -Wextra -Wno-unused-private-field $SANATIZE $OPT -ffp-contract=off -std=c++17 $SRC -o harness
