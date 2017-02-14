#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc

INFO Build Topaz
pushd $SCRIPT_PATH
  load_submodule $SCRIPT_PATH/Topaz
  pushd Topaz
    pip install --upgrade invoke
    export TEST_TYPE=translate-jit-notest
    invoke travis.install_requirements
    invoke travis.run_tests
  popd
popd
OK Topaz Build Completed.
