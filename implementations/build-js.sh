#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`

source $SCRIPT_PATH/script.inc

check_for_node

if [ "$1" = "style" ]
then
  INFO Check style of JavaScript benchmarks
  pushd $SCRIPT_PATH/../benchmarks/JavaScript
  npx eslint .
else
  exit 0
fi
