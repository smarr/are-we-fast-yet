#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
INFO "Build C\# Benchmarks"
pushd $SCRIPT_PATH/../benchmarks/CSharp
if [ "$1" = "style" ]
then
  WARN TODO
else
  dotnet publish -c Release
fi
