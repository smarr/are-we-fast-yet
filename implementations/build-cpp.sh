#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc

if [ "$1" = "style" ]
then
  WARN Disabled for the moment, GHA version seems not to understand .clang-format file format
  exit 0
  
  pushd $SCRIPT_PATH/../benchmarks/C++

  INFO Check Format

  ./build.sh check-format
  FMT_EXIT=$?
  
  INFO Run Lint
  ./build.sh lint
  LNT_EXIT=$?
  exit $((FMT_EXIT + LNT_EXIT))
else
  pushd $SCRIPT_PATH/../benchmarks/C++
  exec ./build.sh
fi
