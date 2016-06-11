#!/bin/bash
set -e # make script fail on first error
pushd `dirname $0` > /dev/null
SCRIPT_PATH=`pwd`
popd > /dev/null

source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

if [ "$1" = "style" ]
then
  INFO Check style of JavaScript benchmarks
  pwd
  ls -lah
  pushd $SCRIPT_PATH/../benchmarks/JavaScript
  pwd
  if [ -e $SCRIPT_PATH/../node_modules/jshint/bin/jshint ]
  then
    $SCRIPT_PATH/../node_modules/jshint/bin/jshint *.js
  else
    jshint *.js
  fi
else
  exit 0
fi
