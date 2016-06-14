#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
export JAVACMD=$GRAAL_CORE_CMD
exec $JRUBY_CMD -Xcompile.invokedynamic=true -J-server -J-XX:+UnlockExperimentalVMOptions -J-XX:+EnableJVMCI -J-XX:+UseJVMCICompiler -J-d64 "$@"
