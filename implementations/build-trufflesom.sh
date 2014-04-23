#!/bin/bash

source ./script.inc
source ./config.inc

INFO Build TruffleSOM
cd TruffleSOM
make
cp ../graal/truffle.jar libs/
OK TruffleSOM Build Completed.
