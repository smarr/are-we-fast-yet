#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
#export JAVACMD=$GRAAL_ENTER_CMD
exec $JRUBY_CMD -J-server -X+T "$@"
