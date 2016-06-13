#!/bin/bash

source ./script.inc
source ./config.inc

INFO Build Graal
cd graal-core
../mx/mx sforceimports

../mx/mx clean
../mx/mx build -p
OK Graal Build Completed.
