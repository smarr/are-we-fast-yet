#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc

check_for_crystal

INFO Build Crystal Benchmarks
pushd $SCRIPT_PATH/../benchmarks/Crystal
if [ "$1" = "style" ]
then
  exit 0
else
  ./build.sh
fi
