#!/bin/bash
if [ "$1" = "style" ]
then
  exit 0
fi

set -e # make script fail on first error
SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source $SCRIPT_PATH/script.inc

pushd $SCRIPT_PATH
load_submodule RSqueak
popd

# TODO: override PyPy of RSqueak with a fixed version once >5.6.0 was released
# if [ \! -d pypy ]; then
#   get_pypy
#   ln -s $SCRIPT_PATH/pypy $SCRIPT_PATH/RSqueak/.build/pypy
# fi

INFO Build RSqueak

export SDL_VIDEODRIVER=dummy
pushd $SCRIPT_PATH/RSqueak

if [ ! -e "$SCRIPT_PATH/pypy2-v5.9.0-linux64.tar.bz2" ]; then
  INFO Get PyPy
  get_web_getter
  $GET https://bitbucket.org/pypy/pypy/downloads/pypy2-v5.9.0-linux64.tar.bz2
  tar xf pypy2-v5.9.0-linux64.tar.bz2
fi

python .build/download_dependencies.py
pypy2-v5.9.0-linux64/bin/pypy   .build/build.py --batch
popd

if [ ! -e "$SCRIPT_PATH/../benchmarks/Smalltalk/RSqueak.image" ]; then
  INFO Get RSqueak Image
  pushd $SCRIPT_PATH
  get_web_getter
  $GET https://www.hpi.uni-potsdam.de/hirschfeld/artefacts/rsqueak/bundle/RSqueak.zip
  unzip -q RSqueak.zip \
           RSqueak.app/Contents/Resources/RSqueak.image \
           RSqueak.app/Contents/Resources/RSqueak.changes \
           RSqueak.app/Contents/Resources/SqueakV50.sources
  mv RSqueak.app/Contents/Resources/RSqueak.image     ../benchmarks/Smalltalk/RSqueak.image
  mv RSqueak.app/Contents/Resources/RSqueak.changes   ../benchmarks/Smalltalk/RSqueak.changes
  mv RSqueak.app/Contents/Resources/SqueakV50.sources ../benchmarks/Smalltalk/
  popd
fi

INFO Using Squeak to build Benchmarking Image
$SCRIPT_PATH/build-squeak32.sh

INFO Building Benchmarking Image from RSqueak.image
cd $SCRIPT_PATH/../benchmarks/Smalltalk
$SCRIPT_PATH/squeak32 RSqueak.image build-image-squeak.st
mv AWFY.image AWFY_RSqueak.image
mv AWFY.changes AWFY_RSqueak.changes
