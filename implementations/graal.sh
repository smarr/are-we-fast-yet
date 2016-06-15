#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
exec $GRAAL_CORE_CMD $GRAAL_JIT_FLAGS "$@"
