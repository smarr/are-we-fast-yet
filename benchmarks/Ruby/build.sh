#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH="$(dirname "$0")"
source "$SCRIPT_PATH/../script.inc"

if [[ "$1" = "style" ]]
then
  INFO Check style of Ruby benchmarks
  pushd "$SCRIPT_PATH"
  rubocop
else
  exit 0
fi
