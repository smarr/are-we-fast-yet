#!/bin/bash

source ./script.inc
source ./config.inc

INFO "Build TruffleSOM (without OMOP)"
cd TruffleSOM
cp ../graal/truffle* libs/
make clean; make
OK "TruffleSOM (without OMOP) Build Completed."

cd ../TruffleSOM-OMOP

INFO "Build TruffleSOM (with OMOP)"

cp ../graal/truffle* libs/
make clean; make
OK "TruffleSOM (with OMOP) Build Completed."
cd ..

