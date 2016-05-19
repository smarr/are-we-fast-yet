#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
export JVMCI_BIN=$GRAAL_VM_BIN
exec $SCRIPT_PATH/SOMns/fast "$@"
