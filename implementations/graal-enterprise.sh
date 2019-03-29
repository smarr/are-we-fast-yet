#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
export JVMCI_BIN=${GRAALVM_HOME}/bin/java

exec ${JVMCI_BIN} $GRAAL_JIT_FLAGS "$@"
