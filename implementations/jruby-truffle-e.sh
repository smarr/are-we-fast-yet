#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
export JAVACMD=$GRAAL_ENTER_CMD
exec $SCRIPT_PATH/jruby/bin/jruby -J-server -X+T "$@"
