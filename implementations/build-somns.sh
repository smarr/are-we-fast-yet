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
  load_git_repo https://github.com/smarr/SOMns.git $SCRIPT_PATH/SOMns
  pushd $SCRIPT_PATH/SOMns
  git submodule update --recursive --init
  ant clobber; ant compile
fi
OK SOMns Build Completed.
