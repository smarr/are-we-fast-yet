#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

INFO Build SOMns
if [ "$1" = "style" ]
then
  exit 0
else
  load_submodule $SCRIPT_PATH/SOMns
  pushd $SCRIPT_PATH/SOMns
  ant clobber; ant compile
fi
OK SOMns Build Completed.
