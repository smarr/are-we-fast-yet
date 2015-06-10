#!/bin/bash

SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc

export JAVA_HOME=$JAVA8_HOME
export EXTRA_JAVA_HOMES=$JAVA7_HOME

exec $SCRIPT_PATH/graal/mxtool/mx --vm server vm "$@"
