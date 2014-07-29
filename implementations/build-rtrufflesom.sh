#!/bin/bash

source ./script.inc
source ./config.inc

cd RPySOM
get_pypy

INFO Build RTruffleSOM
make clean; make RTruffleSOM-jit
OK RTruffleSOM Build Completed.
