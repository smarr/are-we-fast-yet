#!/bin/bash
if [ "$1" = "style" ]
then
  exit 0
fi

set -e # make script fail on first error
SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source $SCRIPT_PATH/script.inc

if [ ! -d "$SCRIPT_PATH/pharo-vm" ]; then
  INFO Get Pharo VM
  pushd $SCRIPT_PATH
  get_web_getter
  $GET get.pharo.org/64/70+vmI || $GET get.pharo.org/64/70+vmI
  bash 70+vmI
  popd
fi

INFO Build Benchmarking Image
cd $SCRIPT_PATH/../benchmarks/Smalltalk
$SCRIPT_PATH/pharo $SCRIPT_PATH/Pharo.image build-image.st
mv $SCRIPT_PATH/AWFY.image AWFY_Pharo.image
mv $SCRIPT_PATH/AWFY.changes AWFY_Pharo.changes
