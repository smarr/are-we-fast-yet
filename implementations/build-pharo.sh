#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

## Get Pharo
get_web_getter
$GET get.pharo.org/vm50 || $GET get.pharo.org/vm50
bash vm50
