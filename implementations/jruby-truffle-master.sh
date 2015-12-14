#!/bin/bash
SCRIPT_PATH=`dirname $0`
GRAAL07_DIR=$SCRIPT_PATH/graalvm-jdk1.8.0
source $SCRIPT_PATH/config.inc

export JAVACMD=$GRAAL07_DIR/bin/java
exec $JRUBY_CMD -X+T "$@"
