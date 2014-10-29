#!/bin/bash

source ./script.inc
source ./config.inc

pushd jruby
INFO "Build JRuby+Truffle"
[ "$(uname)" == "Darwin" ] && GRAAL=openjdk-8-graalvm-b132-macosx-x86_64-0.5.tar.gz
[ "$(expr substr $(uname -s) 1 5)" == "Linux" ] && GRAAL=openjdk-8-graalvm-b132-linux-x86_64-0.5.tar.gz
[ ! -f $GRAAL ] && wget http://lafo.ssw.uni-linz.ac.at/graalvm/$GRAAL
[ ! -d graalvm-jdk1.8.0 ] && tar -zxf $GRAAL
mvn clean install package -Pbootstrap
OK "JRuby+Truffle Build Completed."
popd
