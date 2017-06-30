#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

INFO Build SOM++OMR
if [ "$1" = "style" ]
then
  exit 0
else
  load_submodule $SCRIPT_PATH/SOMppOMR
  pushd $SCRIPT_PATH/SOMppOMR
  git submodule update --init
  chmod +x getLibJitBuilder.sh
  GC_TYPE=omr_gc ./getLibJitBuilder.sh
  make clean || true
  USE_TAGGING=true GC_TYPE=omr_gc CACHE_INTEGER=false DEFAULT_OMR_JIT_ON=true make
  popd
fi
OK SOM++OMR Build Completed.
