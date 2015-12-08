#!/bin/bash
source ./script.inc
source ./config.inc
export JAVA_HOME=$JAVA8_HOME
INFO Build Java Benchmarks
cd ../benchmarks/Java
ant jar
