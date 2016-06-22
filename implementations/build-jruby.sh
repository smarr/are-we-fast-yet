#!/bin/bash
set -e
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

INFO Build JRuby Truffle-head

load_submodule $SCRIPT_PATH/JRuby

cd $SCRIPT_PATH/JRuby
export JAVA_HOME=$JAVA8_HOME
./mvnw clean
./mvnw -q

OK JRuby Build Completed.
