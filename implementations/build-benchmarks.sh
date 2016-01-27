#!/bin/bash
source ./script.inc
source ./config.inc
export JAVA_HOME=$JAVA8_HOME
INFO Build Java Benchmarks
pushd ../benchmarks/Java
ant jar
popd
pushd ../benchmarks/Crystal
./build.sh
popd
