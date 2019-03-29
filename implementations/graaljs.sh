#!/bin/bash
SCRIPT_PATH=`dirname $0`
exec ${GRAALVM_HOME}/bin/node "$@"
