#!/bin/bash

SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc

$JAVA8_HOME/bin/java "$@"
