#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

INFO Build TruffleSOM
if [ "$1" = "style" ]
then
  exit 0
else
  git submodule init $SCRIPT_PATH/TruffleSOM
  git submodule sync --recursive $SCRIPT_PATH/TruffleSOM
  git submodule update $SCRIPT_PATH/TruffleSOM
  pushd $SCRIPT_PATH/TruffleSOM
  make clean; make
  popd
  
  git submodule init $SCRIPT_PATH/TruffleSOM-TOM
  git submodule sync --recursive $SCRIPT_PATH/TruffleSOM-TOM
  git submodule update $SCRIPT_PATH/TruffleSOM-TOM
  pushd $SCRIPT_PATH/TruffleSOM-TOM
  make clean; make
fi
OK TruffleSOM Build Completed.
