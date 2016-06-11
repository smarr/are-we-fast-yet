#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

INFO Build JavaScript benchmarks
if [ "$1" = "style" ]
then
  pushd $SCRIPT_PATH/../benchmarks/JavaScript
  $SCRIPT_PATH/../node_modules/jshint/bin/jshint *.js
else
  exit 0
fi
OK Ruby Benchmark JavaScript Completed.
