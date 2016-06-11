#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

INFO Build Ruby benchmarks
if [ "$1" = "style" ]
then
  pushd $SCRIPT_PATH/../benchmarks/Ruby
  rubocop
else
  exit 0
fi
OK Ruby Benchmark Build Completed.
