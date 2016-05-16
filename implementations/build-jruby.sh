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
