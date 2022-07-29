#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

INFO Build SOM and TruffleSOM
if [ "$1" = "style" ]
then
  exit 0
else
  load_git_repo https://github.com/SOM-st/som-java.git $SCRIPT_PATH/SOM
  pushd $SCRIPT_PATH/SOM
  make clean; make
  popd
  
  load_git_repo https://github.com/SOM-st/TruffleSOM.git $SCRIPT_PATH/TruffleSOM
  pushd $SCRIPT_PATH/TruffleSOM
  make clean; make
  popd
  
  load_git_repo https://github.com/SOM-st/TruffleSOM.git $SCRIPT_PATH/TruffleSOM-TOM truffle-object-model
  pushd $SCRIPT_PATH/TruffleSOM-TOM
  make clean; make
fi
OK TruffleSOM Build Completed.
