#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH="$(dirname "$0")"
source "$SCRIPT_PATH/../script.inc"

INFO Build Node.js Benchmarks
if [[ "$1" = "style" ]]
then
  INFO Check style of JavaScript benchmarks
  pushd "$SCRIPT_PATH"
  npx eslint .
else
  exit 0
fi
