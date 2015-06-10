#!/bin/bash

source ./script.inc
source ./config.inc

INFO Build SOMns
cd SOMns
make clean; make
cp ../graal/truffle.jar libs/
OK SOMns Build Completed.
