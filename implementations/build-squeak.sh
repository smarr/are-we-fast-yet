#!/bin/bash
if [ "$1" = "style" ]
then
  exit 0
fi

set -e # make script fail on first error
SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source $SCRIPT_PATH/script.inc

if [ ! -e "$SCRIPT_PATH/sqcogspurlinux" ]; then
  INFO Get Squeak VM
  pushd $SCRIPT_PATH
  get_web_getter
  $GET http://ftp.squeak.org/5.1/Squeak5.1-16549-32bit/Squeak5.1-16549-32bit-All-in-One.zip
  $GET https://github.com/OpenSmalltalk/opensmalltalk-vm/releases/download/201701281910/cog_linux32x86_squeak.cog.spur_201701281910_itimer.tar.gz
  unzip -q Squeak5.1-16549-32bit-All-in-One.zip
  mv Squeak5.1*.app/Contents/Resources/Squeak5.1*.image   ../benchmarks/Smalltalk/Squeak.image
  mv Squeak5.1*.app/Contents/Resources/Squeak5.1*.changes ../benchmarks/Smalltalk/Squeak.changes
  mv Squeak5.1*.app/Contents/Resources/Squeak*.sources    ../benchmarks/Smalltalk/
  tar xf cog_linux32x86_squeak.cog.spur_201701281910_itimer.tar.gz
  popd
fi

INFO Build Benchmarking Image
cd $SCRIPT_PATH/../benchmarks/Smalltalk
$SCRIPT_PATH/squeak Squeak.image build-image-squeak.st
mv AWFY.image AWFY_Squeak.image
mv AWFY.changes AWFY_Squeak.changes
