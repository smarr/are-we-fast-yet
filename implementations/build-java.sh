#!/bin/bash
source ./script.inc
source ./config.inc
export JAVA_HOME=$JAVA8_HOME
INFO Build Java Benchmarks
pushd ../benchmarks/Java
if [ "$1" = "style" ]
then
  ant checkstyle-jar && ant checkstyle
else
  ant jar
fi
