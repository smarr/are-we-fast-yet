#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

REPO="$SCRIPT_PATH/SOMns-acme"

if [ "$1" = "style" ]
then
  exit 0
else
  INFO Build $REPO
  
  export JVMCI_VERSION_CHECK=ignore

  if [ ! -x "$SCRIPT_PATH/graal-core/bin/java" ]
  then
    ERR Graal Core has not been built yet
    exit 1
  fi
  
  load_submodule $REPO
  pushd $REPO
  ant -Dskip.graal=true clobber
  ant -Dskip.graal=true compile
  
  OK $REPO Build Completed.
fi

