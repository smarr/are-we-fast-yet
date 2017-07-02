#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc
pushd $SCRIPT_PATH/../benchmarks/Lua
if [ "$1" = "style" ]
then
  luacheck *.lua
fi
