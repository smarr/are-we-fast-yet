#!/bin/sh
SCRIPT_PATH=`dirname $0`
. /home/gitlab-runner/.local/emsdk/emsdk_set_env.sh

pushd ${SCRIPT_PATH}/CSOM-emscripten/
make clobber
make em-awfy
popd
