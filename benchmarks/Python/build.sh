#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH="$(dirname "$0")"
source "$SCRIPT_PATH/../script.inc"

if [[ "$1" = "style" ]]
then
  INFO Check style of Python benchmarks
  pushd "$SCRIPT_PATH"
  if [[ "$2" != "skip-black-for-python" ]]; then
    python -m black --check --diff .
  fi
  python -m pylint ./*.py som
else
  exit 0
fi
