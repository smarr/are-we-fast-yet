#!/bin/bash
set -e # make script fail on first error

source script.inc

check_for_tools git ant tar make javac mv unzip uname cc c++
get_web_getter

./build-java.sh
./build-crystal.sh

./build-jruby.sh
./build-somns.sh
./build-trufflesom.sh

./build-benchmarks.sh

# get_jdk9ea # disabled since Nashorn is only user and not really supported currently

# ./build-graal.sh

# ./build-rpysom.sh
# ./build-rtrufflesom.sh

# ./build-sompp.sh
# ./build-som.sh

# ./build-csom.sh
# ./build-luajit.sh

OK done.
