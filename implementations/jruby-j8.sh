#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
export JAVACMD=$JAVA8_HOME/bin/java
exec $JRUBY_CMD -J-server -X-T -Xcompile.invokedynamic=true "$@"
