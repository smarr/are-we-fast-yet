#!/bin/bash
set -e
SCRIPT_PATH=`dirname $0`
pushd $SCRIPT_PATH
crystal build --release harness.cr
