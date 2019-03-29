#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
export JAVACMD=${GRAALVM_HOME}/bin/java

exec $JRUBY_CMD -X+T -J-server -J-XX:+UnlockExperimentalVMOptions -J-XX:+EnableJVMCI -J-d64 "$@"
