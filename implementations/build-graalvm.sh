#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

INFO Setup GraalVM

if [ -x "$GRAALVM_HOME/bin/java" ]
then
  if [ -d "$SCRIPT_PATH/graalvm" ]
  then
    rm $SCRIPT_PATH/graalvm
  fi
  ln -s "$GRAALVM_HOME" $SCRIPT_PATH/graalvm
fi

OK GraalVM Setup and available in "$SCRIPT_PATH/graalvm"
