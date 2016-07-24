#!/bin/bash
if [ "$1" = "style" ]
then
  exit 0
fi

set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc



INFO Get Pharo VM

get_web_getter
$GET get.pharo.org/vm50 || $GET get.pharo.org/vm50
bash vm50

INFO Get Pharo Image
cd $SCRIPT_PATH/../benchmarks/Smalltalk
$GET get.pharo.org/stable || $GET get.pharo.org/stable
bash stable

INFO Build Benchmarking Image
$SCRIPT_PATH/pharo Pharo.image build-image.st
