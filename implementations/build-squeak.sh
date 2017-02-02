#!/bin/bash
if [ "$1" = "style" ]
then
  exit 0
fi

set -e # make script fail on first error
SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source $SCRIPT_PATH/script.inc

if [ ! -e "$SCRIPT_PATH/squeak.sh" ]; then
  INFO Get Squeak VM
  pushd $SCRIPT_PATH
  get_web_getter
  $GET http://ftp.squeak.org/5.1/Squeak5.1-16549-32bit/Squeak5.1-16549-32bit-All-in-One.zip
  unzip -q Squeak5.1-16549-32bit-All-in-One.zip
  cp Squeak5.1*.app/Contents/Resources/Squeak5.1*.image ../benchmarks/Smalltalk/Squeak.image
  cp Squeak5.1*.app/Contents/Resources/Squeak5.1*.changes ../benchmarks/Smalltalk/Squeak.changes
  cp Squeak5.1*.app/Contents/Resources/Squeak*.sources ../benchmarks/Smalltalk/

  # This can be dropped once https://github.com/travis-ci/travis-build/pull/879 is merged and in production
  if [[ "$(uname -s)" = "Linux" ]]; then
      wget -q https://raw.githubusercontent.com/hpi-swa/smalltalkCI/master/utils/set_rtprio_limit.c
      gcc -o set_rtprio_limit set_rtprio_limit.c
      chmod +x ./set_rtprio_limit
      sudo ./set_rtprio_limit $$
  else
      echo "#!/bin/bash" > set_rtprio_limit
      chmod +x set_rtprio_limit
  fi
  popd
fi

INFO Build Benchmarking Image
cd $SCRIPT_PATH/../benchmarks/Smalltalk
$SCRIPT_PATH/squeak Squeak.image build-image-squeak.st
mv AWFY.image AWFY_Squeak.image
mv AWFY.changes AWFY_Squeak.changes
