#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
export JAVACMD=$GRAAL_VM_BIN
exec $JRUBY_CMD -J-server -X+T "$@"
