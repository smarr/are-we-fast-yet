#!/bin/bash

SCRIPT_PATH=`dirname $0`

# export JAVA_OPTS=-G:+TraceTruffleCompilationHistogram 
export JAVACMD=$SCRIPT_PATH/graal/jdk1.8.0_05/product/bin/java

$SCRIPT_PATH/jruby/bin/jruby -J-server -X+T -J-G:-TruffleSplittingEnabled "$@"

