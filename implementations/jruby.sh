#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
export JAVACMD=$GRAAL_BASIC_CMD
exec $JRUBY_CMD -J-server -Xcompile.invokedynamic "$@"
