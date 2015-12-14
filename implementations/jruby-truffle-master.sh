#!/bin/bash
SCRIPT_PATH=`dirname $0`
GRAAL07_DIR=$SCRIPT_PATH/graalvm-jdk1.8.0
export JAVACMD=$GRAAL07_DIR/bin/java
exec $SCRIPT_PATH/JRuby-master/bin/jruby -X+T "$@"
