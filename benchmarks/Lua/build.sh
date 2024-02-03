#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH="$(dirname "$0")"
source $SCRIPT_PATH/../script.inc

if [[ "$1" = "style" ]]
then
  INFO Check style of Lua benchmarks
  pushd "$SCRIPT_PATH"
  luacheck ./*.lua
else
  exit 0
fi
