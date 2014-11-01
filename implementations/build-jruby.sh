#!/bin/bash

source ./script.inc
source ./config.inc

pushd jruby
INFO "Build JRuby+Truffle"

INFO Get Graal binary
if [ "$(uname)" == "Darwin" ]
then
  GRAAL=openjdk-8-graalvm-b132-macosx-x86_64-0.5.tar.gz
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]
then
  GRAAL=openjdk-8-graalvm-b132-linux-x86_64-0.5.tar.gz
fi

[ ! -f $GRAAL ] && wget http://lafo.ssw.uni-linz.ac.at/graalvm/$GRAAL
[ ! -d graalvm-jdk1.8.0 ] && tar -zxf $GRAAL
INFO Got Graal binary

INFO Compile JRuby
mvn clean install package -Pbootstrap
OK "JRuby+Truffle Build Completed."
popd
