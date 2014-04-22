#!/bin/bash

SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc

$JAVA7_HOME/bin/java "$@"
