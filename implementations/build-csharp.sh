#!/bin/bash
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
INFO "Build C# Benchmarks"
pushd $SCRIPT_PATH/../benchmarks/CSharp
if [ "$1" = "style" ]
then
  dotnet tool run jb inspectcode Benchmarks.csproj --build -f=Text -o=report.txt --include=**/*.cs
  cat report.txt
  dotnet tool run jb cleanupcode .
else
  dotnet publish -c Release
fi
