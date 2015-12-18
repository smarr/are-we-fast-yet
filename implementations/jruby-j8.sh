#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
export JAVACMD=$JAVA8_HOME/bin/java
exec $JRUBY_CMD -Xcompile.invokedynamic=true "$@"
