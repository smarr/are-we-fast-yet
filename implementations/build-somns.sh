#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

INFO Build SOMns
if [ "$1" = "style" ]
then
  exit 0
else
  git submodule init $SCRIPT_PATH/SOMns
  git submodule sync --recursive $SCRIPT_PATH/SOMns
  git submodule update $SCRIPT_PATH/SOMns
  pushd $SCRIPT_PATH/SOMns
  make clean; make
fi
OK SOMns Build Completed.
