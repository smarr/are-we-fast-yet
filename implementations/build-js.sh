#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`

source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

if [ "$1" = "style" ]
then
  INFO Check style of JavaScript benchmarks
  set -v
  set +e
  pwd
  echo "SCRIPT_PATH: $SCRIPT_PATH"
  ls -lah
  pushd $SCRIPT_PATH/../benchmarks/JavaScript
  pwd
  ls -lah $SCRIPT_PATH/../node_modules/
  ls -lah $SCRIPT_PATH/../node_modules/jshint
  if [ -e $SCRIPT_PATH/../node_modules/jshint/bin/jshint ]
  then
    $SCRIPT_PATH/../node_modules/jshint/bin/jshint *.js
  else
    jshint *.js
  fi
else
  exit 0
fi
