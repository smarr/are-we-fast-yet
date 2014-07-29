#!/bin/bash

source ./script.inc
source ./config.inc

INFO Build TruffleSOM
cd TruffleSOM
make clean; make
cp ../graal/truffle* libs/
OK TruffleSOM Build Completed.
