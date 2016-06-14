#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

INFO Build Graal
load_submodule $SCRIPT_PATH/mx
load_submodule $SCRIPT_PATH/graal-core
cd $SCRIPT_PATH/graal-core
../mx/mx sforceimports

../mx/mx clean
../mx/mx build -p
OK Graal Build Completed.
