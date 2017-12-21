#!/bin/bash
set -e
SCRIPT_PATH=`dirname $0`
pushd $SCRIPT_PATH
crystal build --release --no-debug harness.cr
