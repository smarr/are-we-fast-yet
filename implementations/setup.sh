#!/bin/bash

source script.inc

check_for_tools git ant tar make javac mv unzip uname
get_web_getter

./build-graal.sh
./build-classic-benchmarks.sh
./build-trufflesom.sh
./build-rtrufflesom.sh

OK done.
exit 0;

if [ \! \( -x /usr/bin/pharo-vm-nox -o -d pharo \) ]; then
  INFO -n Get Pharo VM
  TMP_OS=`uname | tr "[:upper:]" "[:lower:]"`
  if [[ "{$TMP_OS}" = *darwin* ]]; then
    OS="mac";
  elif [[ "{$TMP_OS}" = *linux* ]]; then
    OS="linux";
  elif [[ "{$TMP_OS}" = *win* ]]; then
    OS="win";
  elif [[ "{$TMP_OS}" = *mingw* ]]; then
    OS="win";
  else
    ERR "Unsupported OS";
    exit 1;
  fi
  VM_URL="http://files.pharo.org/vm/pharo/${OS}/stable.zip"
  $GET $VM_URL
  mkdir -p pharo
  mv stable.zip pharo
  cd pharo
  unzip stable.zip
  cd ..
else
  OK Got Pharo VM
fi

if [ \! -f Pharo-bench.image ]; then
  INFO -n Get Pharo Benchmark Image
  $GET http://files.pharo.org/image/PharoV20.sources.zip
  $GET https://ci.inria.fr/rmod/view/Mate/job/Pharo-bench/lastSuccessfulBuild/artifact/Pharo-bench.zip
  unzip PharoV20.sources.zip
  unzip Pharo-bench.zip
else
  OK Got Pharo Benchmark Image
fi

if [ \! -d SOMpp ]; then
  INFO Get SOM++
  git clone --recursive https://github.com/SOM-st/SOMpp.git
  cd SOMpp
  make
  cd ..
else
  OK Got SOM++
fi


