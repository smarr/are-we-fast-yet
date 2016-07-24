#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc

## Check for requirements
check_for_tools git ant make mv uname cc c++
check_for_crystal "non-fatal"
check_for_node    "non-fatal"
check_for ruby2.3 "" "non-fatal"
check_for ruby    "Please see https://www.ruby-lang.org/en/documentation/installation/" "non-fatal"
check_for_graalvm "non-fatal"

$SCRIPT_PATH/build-java.sh
$SCRIPT_PATH/build-crystal.sh
$SCRIPT_PATH/build-jruby.sh
$SCRIPT_PATH/build-som.sh
$SCRIPT_PATH/build-somns.sh
# $SCRIPT_PATH/build-graal.sh
$SCRIPT_PATH/build-pharo.sh


if [ -e "$GRAALVM_ARCHIVE" ]; then
  mkdir -p $SCRIPT_PATH/graalvm
  tar xf $GRAALVM_ARCHIVE --strip-components 1 -C $SCRIPT_PATH/graalvm
fi

# get_jdk9ea # disabled since Nashorn is only user and not really supported currently

# 

# ./build-rpysom.sh
# ./build-rtrufflesom.sh

# ./build-sompp.sh
# ./build-som.sh

# ./build-csom.sh
# ./build-luajit.sh

OK done.
