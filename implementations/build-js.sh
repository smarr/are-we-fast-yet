#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

if [ "$1" = "style" ]
then
  INFO Check style of JavaScript benchmarks
  pwd
  ls -lah
  pushd $SCRIPT_PATH/../benchmarks/JavaScript
  pwd
  $SCRIPT_PATH/../node_modules/jshint/bin/jshint *.js
else
  exit 0
fi
