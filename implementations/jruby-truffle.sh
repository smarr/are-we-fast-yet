#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
export JAVACMD=$GRAAL_CORE_CMD
exec $JRUBY_CMD -X+T -J-server -J-XX:+UnlockExperimentalVMOptions -J-XX:+EnableJVMCI -J-d64 "$@"
