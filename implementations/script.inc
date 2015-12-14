#!/bin/bash

# set up color commands
if [ -t 1 -a -t 2 -a \( `type -t tput` = "file" \) ]; then
  _colors=`tput colors`
  if [ "$_colors" -ge 256 ]; then
    INFO () { tput setaf 33;  /bin/echo "$@"; tput sgr0; }
    OK   () { tput setaf 28;  /bin/echo "$@"; tput sgr0; }
    WARN () { tput setaf 226; /bin/echo "$@"; tput sgr0; }
    ERR  () { tput setaf 196; /bin/echo "$@"; tput sgr0; }
  else
    INFO () { tput setaf 4; /bin/echo "$@"; tput sgr0; }
    OK   () { tput setaf 2; /bin/echo "$@"; tput sgr0; }
    WARN () { tput setaf 3; /bin/echo "$@"; tput sgr0; }
    ERR  () { tput setaf 1; /bin/echo "$@"; tput sgr0; }
  fi
else
  INFO () { /bin/echo "$@"; }
  OK   () { /bin/echo "$@"; }
  WARN () { /bin/echo "$@"; }
  ERR  () { /bin/echo "$@"; }
fi


function get_web_getter() {
  # get a getter
  if [ \! -z `type -t curl` ]; then
    GET="curl --silent --location --compressed -O"
  elif [ \! -z `type -t wget` ]; then
    GET="wget --quiet"
  else
    ERR "No getter (curl/wget)"
    exit 1
  fi
  OK Getter is \"$GET\"
}

function check_for_tools() {
  INFO -n 'Tools: '
  for tool in $@; do
    if [ -x `type -t $tool` ]; then
      WARN -n No $tool
    else
      INFO -n "$tool "
    fi
  done
  OK ok.
}

function get_pypy() {
  if [ \! -d pypy ]; then
    get_web_getter
    
    INFO Get PyPy Source
    PYPY_VERSION="2.5.1"
    DOWNLOAD_URL="https://bitbucket.org/pypy/pypy/downloads/pypy-$PYPY_VERSION-src.tar.bz2"
    $GET $DOWNLOAD_URL || $GET $DOWNLOAD_URL
    tar -xjf pypy-$PYPY_VERSION-src.tar.bz2
    mv pypy-$PYPY_VERSION-src pypy
  else
    OK Got PyPy Source
  fi
}

function get_graal07() {
  GRAAL07_ARCHIVE=openjdk-8-graalvm-b132-linux-x86_64-0.7.tar.gz
  GRAAL07_URL=http://lafo.ssw.uni-linz.ac.at/graalvm/$GRAAL07_ARCHIVE
  GRAAL07_DIR=graalvm-jdk1.8.0

  INFO Get Graal 0.7 from $GRAAL07_URL
  if [ \! -d "$GRAAL07_DIR" ]; then
    $GET $GRAAL07_URL || $GET $GRAAL07_URL
    tar -xjf $GRAAL07_ARCHIVE
    OK Got Graal 0.7
  else
    OK Have Graal 0.7
  fi
}

function get_jdk9ea() {
  JDK_ARCHIVE=jdk-9-ea+96_linux-x64_bin.tar.gz
  JDK_URL=http://www.java.net/download/jdk9/archive/96/binaries/$JDK_ARCHIVE
  JDK_DIR=jdk-9
  
  INFO Get JDK9 Early Access from $JDK_URL
  if [ \! -d "$JDK_DIR" ]; then
    $GET $JDK_URL || $GET $JDK_URL
    tar -xjf $JDK_ARCHIVE
    OK Got JDK Early Access
  else
    OK Have JDK Early Access
  fi
}
