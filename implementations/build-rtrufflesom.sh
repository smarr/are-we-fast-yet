#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

INFO Build RTruffleSOM
if [ "$1" = "style" ]
then
  exit 0
else
  load_submodule $SCRIPT_PATH/RTruffleSOM
  pushd $SCRIPT_PATH/RTruffleSOM
  get_pypy
  make clean
  make -j4 RTruffleSOM-jit RTruffleSOM-no-jit
  popd
  
  load_submodule $SCRIPT_PATH/TruffleSOM-TOM
  pushd $SCRIPT_PATH/TruffleSOM-TOM
  make clean; make
fi
OK RTruffleSOM Build Completed.
