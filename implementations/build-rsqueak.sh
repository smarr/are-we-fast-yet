#!/bin/bash
if [ "$1" = "style" ]
then
  exit 0
fi

set -e # make script fail on first error
SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source $SCRIPT_PATH/script.inc

if [ ! -e "$SCRIPT_PATH/RSqueak.sh" ]; then
  INFO Get RSqueak Image
  pushd $SCRIPT_PATH
  get_web_getter
  $GET https://www.hpi.uni-potsdam.de/hirschfeld/artefacts/rsqueak/bundle/RSqueak.zip
  unzip -q RSqueak.zip
  cp RSqueak.app/Contents/Resources/RSqueak.image ../benchmarks/Smalltalk/RSqueak.image
  cp RSqueak.app/Contents/Resources/RSqueak.changes ../benchmarks/Smalltalk/RSqueak.changes
  cp RSqueak.app/Contents/Resources/Squeak*.sources ../benchmarks/Smalltalk/
  popd
fi

INFO Using Squeak to build Benchmarking Image
$SCRIPT_PATH/build-squeak.sh

INFO Building Benchmarking Image from RSqueak.image
cd $SCRIPT_PATH/../benchmarks/Smalltalk
$SCRIPT_PATH/squeak RSqueak.image build-image-squeak.st
mv AWFY.image AWFY_RSqueak.image
mv AWFY.changes AWFY_RSqueak.changes
