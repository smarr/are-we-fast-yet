#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
export JVMCI_BIN=${GRAALVM_HOME}/bin/java
export GRAAL_FLAGS=$GRAAL_HOSTED_FLAGS
exec $SCRIPT_PATH/SOMns/fast -E "$@"
