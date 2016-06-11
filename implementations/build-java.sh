#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc
export JAVA_HOME=$JAVA8_HOME
INFO Build Java Benchmarks
pushd $SCRIPT_PATH/../benchmarks/Java
if [ "$1" = "style" ]
then
  ant checkstyle-jar && ant checkstyle
else
  ant jar
fi
