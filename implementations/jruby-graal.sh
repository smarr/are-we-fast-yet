#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
export JAVACMD=$GRAAL_GRAAL_BASIC_CMD
exec $SCRIPT_PATH/jruby/bin/jruby -J-server -Xcompile.invokedynamic "$@"
