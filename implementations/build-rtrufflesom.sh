#!/bin/bash

source ./script.inc
source ./config.inc

cd RTruffleSOM

if [ \! -d pypy ]; then
  INFO -n Get PyPy Source
  $GET https://bitbucket.org/pypy/pypy/downloads/pypy-2.2.1-src.tar.bz2 || $GET https://bitbucket.org/pypy/pypy/downloads/pypy-2.2.1-src.tar.bz2
  tar -xjf pypy-2.2.1-src.tar.bz2
  mv pypy-2.2.1-src pypy
else
  OK Got PyPy Source
fi

INFO Build RTruffleSOM
make RTruffleSOM-jit
OK RTruffleSOM Build Completed.
