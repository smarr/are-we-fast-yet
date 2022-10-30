#!/bin/bash
set -e
pushd `dirname $0`
crystal build --release --no-debug harness.cr
