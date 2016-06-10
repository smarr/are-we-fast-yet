#!/bin/bash
source ./script.inc
source ./config.inc
export JAVA_HOME=$JAVA8_HOME
INFO Build Crystal Benchmarks
pushd ../benchmarks/Crystal
if [ "$1" = "style" ]
then
  exit 0
else
  ./build.sh
fi
