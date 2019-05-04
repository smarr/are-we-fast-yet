#!/bin/bash
if [ "$1" = "style" ]
then
  exit 0
fi

set -e # make script fail on first error
SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source $SCRIPT_PATH/script.inc

if [ ! -e "$SCRIPT_PATH/sqcogspur64linuxht" ]; then
  INFO Get Squeak VM
  pushd $SCRIPT_PATH
  get_web_getter
  $GET http://files.squeak.org/5.2/Squeak5.2-18229-64bit/Squeak5.2-18229-64bit-All-in-One.zip
  $GET https://github.com/OpenSmalltalk/opensmalltalk-vm/releases/download/201810190412/squeak.cog.spur_linux64x64_201810190412.tar.gz
  unzip -q Squeak5.2-18229-64bit-All-in-One.zip
  mv Squeak5.2*.app/Contents/Resources/Squeak5.2*.image   ../benchmarks/Smalltalk/Squeak.image
  mv Squeak5.2*.app/Contents/Resources/Squeak5.2*.changes ../benchmarks/Smalltalk/Squeak.changes
  mv Squeak5.2*.app/Contents/Resources/Squeak*.sources    ../benchmarks/Smalltalk/
  tar xf squeak.cog.spur_linux64x64_201810190412.tar.gz
  popd
fi

INFO Build 64bit Benchmarking Image
cd $SCRIPT_PATH/../benchmarks/Smalltalk
$SCRIPT_PATH/squeak64 Squeak.image build-image-squeak.st
mv AWFY.image AWFY_Squeak64.image
mv AWFY.changes AWFY_Squeak64.changes
