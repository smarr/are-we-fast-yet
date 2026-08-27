#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH="$(dirname "$0")"
source "$SCRIPT_PATH/../script.inc"
pushd "$SCRIPT_PATH"

if [[ "$1" = "style" ]]
then
  INFO Check style of C# Benchmarks
  dotnet tool restore
  dotnet jb inspectcode Benchmarks.slnx --format=Text --output=-
else
  INFO Build C# Benchmarks
  dotnet build --no-incremental
fi
