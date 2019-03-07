#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
exec ${GRAALVM_HOME}/bin/node "$@"
