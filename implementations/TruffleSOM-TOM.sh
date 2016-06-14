#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
export JVMCI_BIN=$GRAAL_CORE_CMD
export GRAAL_FLAGS=$GRAAL_HOSTED_FLAGS
exec $SCRIPT_PATH/TruffleSOM-TOM/som "$@"
