#!/bin/bash
SCRIPT_PATH=`dirname $0`
exec $SCRIPT_PATH/jdk-9/bin/jjs "$@"
