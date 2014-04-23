#!/bin/bash

source script.inc

check_for_tools git ant tar make javac mv unzip uname
get_web_getter

./build-graal.sh
./build-classic-benchmarks.sh
./build-trufflesom.sh

exit 1.



if [ \! -d pypy ]; then
  INFO -n Get PyPy Source
  $GET https://bitbucket.org/pypy/pypy/downloads/pypy-2.2.1-src.tar.bz2 || $GET https://bitbucket.org/pypy/pypy/downloads/pypy-2.2.1-src.tar.bz2
  tar -xjf pypy-2.2.1-src.tar.bz2
  mv pypy-2.2.1-src pypy
else
  OK Got PyPy Source
fi

if [ \! \( -f RTruffleSOM/RTruffleSOM-no-jit \) ]; then
  INFO -n Compile RTruffleSOMs
  cd RTruffleSOM
  PYTHONPATH=$PYTHONPATH:../pypy ../pypy/rpython/bin/rpython --batch src/targetsomstandalone.py
  PYTHONPATH=$PYTHONPATH:../pypy ../pypy/rpython/bin/rpython --batch -Ojit src/targetsomstandalone.py
  cd ..

else
  OK Got RTruffleSOMs
fi


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

if [ \! \( -d TruffleSOM-exp \) ]; then
  INFO Get TruffleSOM experiments
  git clone --recursive https://github.com/smarr/TruffleSOM.git TruffleSOM-exp-source
  mkdir TruffleSOM-exp
  cd TruffleSOM-exp-source

  for exp in without-extra-block-with-context       \
             without-local-nonlocal-distinction     \
             without-catch-nonlocalreturn-node      \
             without-execute-void                   \
             without-control-specialization         \
             without-eager-primitives               \
             without-lookup-caching                 \
             without-global-lookup-caching          \
             without-explicit-frame-initialization  \
             only-generic-field-access              \
             only-generic-var-access; do
    git checkout -b $exp origin/opt-exp/$exp
    ant clean
    ant jar
    cp build/som.jar ../TruffleSOM-exp/som.$exp.jar
  done
  cd ..
else
  OK Folder TruffleSOM-exp exists, assume all experiments prepared.
fi

OK done.
exit 0;
