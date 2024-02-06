#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH="$(dirname "$0")"
source "$SCRIPT_PATH/../script.inc"

INFO Build Node.js Benchmarks
pushd "$SCRIPT_PATH"

if [[ "$1" = "style" ]]
then
  INFO Check style of JavaScript benchmarks

  npx eslint .
else
  if [[ ! -d "node_modules" ]]
  then
    npm install
  fi
  npm run webpack
fi
