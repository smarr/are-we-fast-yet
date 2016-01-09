#!/bin/bash

source ./script.inc
source ./config.inc

INFO Build TruffleSOM
cd TruffleSOM
make clean; make
OK TruffleSOM Build Completed.

INFO Build TruffleSOM with TruffleObjectModel
cd TruffleSOM-TOM
make clean; make
OK TruffleSOM with TruffleObjectModel Build Completed.
