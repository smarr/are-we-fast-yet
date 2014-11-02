#!/bin/bash
SCRIPT_PATH=`dirname $0`
JAVACMD=$SCRIPT_PATH/jruby/graalvm-jdk1.8.0/bin/java $SCRIPT_PATH/jruby/bin/jruby "$@"
