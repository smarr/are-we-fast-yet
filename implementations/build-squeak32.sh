#!/bin/bash
if [ "$1" = "style" ]
then
  exit 0
fi

set -e # make script fail on first error
SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source $SCRIPT_PATH/script.inc

if [ ! -e "$SCRIPT_PATH/sqcogspurlinuxht" ]; then
  INFO Get Squeak VM
  pushd $SCRIPT_PATH
  get_web_getter
  $GET http://files.squeak.org/5.2/Squeak5.2-18229-32bit/Squeak5.2-18229-32bit-All-in-One.zip
  $GET https://github.com/OpenSmalltalk/opensmalltalk-vm/releases/download/201810190412/squeak.cog.spur_linux32x86_201810190412.tar.gz
  unzip -q Squeak5.2-18229-32bit-All-in-One.zip
  mv Squeak5.2*.app/Contents/Resources/Squeak5.2*.image   ../benchmarks/Smalltalk/Squeak.image
  mv Squeak5.2*.app/Contents/Resources/Squeak5.2*.changes ../benchmarks/Smalltalk/Squeak.changes
  mv Squeak5.2*.app/Contents/Resources/Squeak*.sources    ../benchmarks/Smalltalk/
  tar xf squeak.cog.spur_linux32x86_201810190412.tar.gz
  popd
fi

INFO Build 32bit Benchmarking Image
cd $SCRIPT_PATH/../benchmarks/Smalltalk
$SCRIPT_PATH/squeak32 Squeak.image build-image-squeak.st
mv AWFY.image AWFY_Squeak32.image
mv AWFY.changes AWFY_Squeak32.changes
