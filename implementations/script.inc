#!/bin/bash

# make SCRIPT_PATH absolute
pushd `dirname $0` > /dev/null
SCRIPT_PATH=`pwd`
popd > /dev/null

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
    PYPY_VERSION="5.8.0"
    PYPY_DIR="pypy2-v${PYPY_VERSION}-src"
    PYPY_TAR="${PYPY_DIR}.tar.bz2"
    DOWNLOAD_URL="https://bitbucket.org/pypy/pypy/downloads/${PYPY_TAR}"
    $GET $DOWNLOAD_URL || $GET $DOWNLOAD_URL
    tar -xjf $PYPY_TAR
    mv $PYPY_DIR pypy
  else
    OK Got PyPy Source
  fi
}

function get_jdk9ea() {
  JDK_ARCHIVE=jdk-9-ea+96_linux-x64_bin.tar.gz
  JDK_URL=http://www.java.net/download/jdk9/archive/96/binaries/$JDK_ARCHIVE
  JDK_DIR=jdk-9
  
  INFO Get JDK9 Early Access from $JDK_URL
  if [ \! -d "$JDK_DIR" ]; then
    $GET $JDK_URL || $GET $JDK_URL
    tar -xzf $JDK_ARCHIVE
    OK Got JDK Early Access
  else
    OK Have JDK Early Access
  fi
}

function load_git_repo() {
  INFO Cloning "$1" into "$2"
  if [ -z "$3" ]
  then
    git clone --depth=1 "$1" "$2"
  else
    INFO Using Branch "$3"
    git clone --branch="$3" --depth=1 "$1" "$2"
  fi
}

function load_submodule() {
  ERR Submodules are not longer used. Please replace the use of "$1" with load_git_repo for the relevant repo and branch.
}

function check_for() {
  if [ ! -x `which $1` ]
  then
    ERR "$1 binary not found. $2"
    if [ "non-fatal" -ne "$3" ]
    then
      exit 1
    fi
  fi
}

function check_for_crystal() {
  check_for "crystal" "Please see http://crystal-lang.org/docs/installation/index.html" "$1"
}

function check_for_node() {
  check_for "node" "Please see https://nodejs.org/en/download/" "$1"
}

function check_for_graalvm() {
  if [ ! -x "$SCRIPT_PATH/graalvm/jre/bin/javao" ]
  then
    ERR "GraalVM not found."
    ERR "To run benchmarks on the GraalVM, please see 'implementations/graalvm/README.md'"
  fi
}

