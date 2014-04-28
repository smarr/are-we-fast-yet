#!/bin/bash

source ./script.inc
source ./config.inc

cd RPySOM
get_pypy

INFO Build RPySOM
make clean; make RPySOM-jit
OK RPySOM Build Completed.
