#!/bin/bash

source ./script.inc
source ./config.inc

export JAVA_HOME=$JAVA8_HOME

INFO Build Cost-Of-Reflection Benchmarks

cd cost-of-reflection/java/reflection
mvn clean
mvn package


OK Cost-Of-Reflection Benchmarks Build Completed.
