#!/bin/bash

source ./script.inc
source ./config.inc

INFO "Build TruffleSOM (without OMOP)"
cd TruffleSOM
make clean; make
cp ../graal/truffle* libs/
OK "TruffleSOM (without OMOP) Build Completed."

cd ../TruffleSOM-OMOP

INFO "Build TruffleSOM (with OMOP)"

make clean; make
cp ../graal/truffle* libs/
OK "TruffleSOM (with OMOP) Build Completed."
cd ..

