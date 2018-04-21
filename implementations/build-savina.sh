#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc
export JAVA_HOME=$JAVA8_HOME

INFO Build Savina Benchmarks
pushd $SCRIPT_PATH/savina

if [ "$1" = "style" ]
then
  exit 0
else
  mvn package
fi
