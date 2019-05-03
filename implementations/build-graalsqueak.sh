#!/bin/bash
if [ "$1" = "style" ]
then
  exit 0
fi

set -e # make script fail on first error
readonly SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_PATH}/script.inc"

readonly GRAALSQUEAK_DIR="${SCRIPT_PATH}/GraalSqueak"
readonly GRAALSQUEAK_JAR="https://github.com/hpi-swa/graalsqueak/releases/download/awfy/graalsqueak-component.jar"
readonly GRAALVM_VERSION=1.0.0-rc16
readonly GRAALVM_TARBALL=graalvm-ce-${GRAALVM_VERSION}-linux-amd64.tar.gz

INFO Building GraalSqueak

mkdir "${GRAALSQUEAK_DIR}"
pushd "${GRAALSQUEAK_DIR}"

curl -sSL --retry 3 -o ${GRAALVM_TARBALL} https://github.com/oracle/graal/releases/download/vm-${GRAALVM_VERSION}/${GRAALVM_TARBALL}
tar -xzf ${GRAALVM_TARBALL}
readonly GRAALVM_HOME=$(pwd)/graalvm-ce-${GRAALVM_VERSION}

"${GRAALVM_HOME}/bin/gu" install -u "${GRAALSQUEAK_JAR}"

popd

INFO Using Squeak to build benchmarking image
$SCRIPT_PATH/build-squeak.sh

INFO Copying benchmarking image for GraalSqueak
cd $SCRIPT_PATH/../benchmarks/Smalltalk
mv AWFY_Squeak.image AWFY_GraalSqueak.image
mv AWFY_Squeak.changes AWFY_GraalSqueak.changes
