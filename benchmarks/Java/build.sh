#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH="$(dirname "$0")"
source "$SCRIPT_PATH/../script.inc"
pushd "$SCRIPT_PATH"

if [[ "$1" = "style" ]]
then
  ant checkstyle-jar && ant checkstyle
else
  INFO Build Java Benchmarks
  ant jar
fi
