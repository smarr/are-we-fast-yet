#!/bin/bash

source ./script.inc
source ./config.inc

get_pypy

cd RTruffleSOM
INFO "Build RTruffleSOM (without OMOP)"
make clean; make RTruffleSOM-jit &
OK "RTruffleSOM (without OMOP) Build Completed."

cd ../RTruffleSOM-OMOP
INFO "Build RTruffleSOM (with OMOP)"
make clean; make RTruffleSOM-jit
OK "RTruffleSOM (with OMOP) Build Completed."
