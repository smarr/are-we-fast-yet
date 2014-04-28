#!/bin/bash

source ./script.inc
source ./config.inc


ERR Hack Maven, force install truffle.jar
cp graal/truffle.jar ~/.m2/repository/com/oracle/truffle/0.2/truffle-0.2.jar

INFO Build JRuby
cd jruby
mvn package
OK JRuby Build Completed.
