#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/config.inc
echo $MX -p $GRAAL_JS --jdk jvmci --dynamicimport graal-enterprise node "$@"
echo `pwd`
exec $MX -p $GRAAL_JS --jdk jvmci --dynamicimport graal-enterprise node "$@"
