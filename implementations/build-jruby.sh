#!/bin/bash

source ./script.inc
source ./config.inc

INFO Build JRuby Truffle-head
cd JRuby
./mvnw

INFO Build JRuby Master
cd ../JRuby-master
./mvnw

OK JRuby Build Completed.

INFO "Get Graal 0.7 for JRuby"
cd ..

get_graal07
