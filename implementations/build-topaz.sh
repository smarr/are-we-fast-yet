#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc

INFO Build Topaz
pushd $SCRIPT_PATH
  load_submodule $SCRIPT_PATH/Topaz
  pushd Topaz
    pip install invoke==0.15.0
    export TEST_TYPE=translate-jit-notest
    # requirements don't fix invoke, so, make sure we have the right version
    invoke travis.install_requirements
    pip install invoke
    pip install invoke==0.15.0
    invoke travis.run_tests
  popd
popd
OK Topaz Build Completed.
