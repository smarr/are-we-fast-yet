#!/bin/bash

source ./script.inc
source ./config.inc

INFO Build JRuby
cd jruby
mvn package
OK JRuby Build Completed.
