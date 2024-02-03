#!/bin/bash

# make SCRIPT_PATH absolute
pushd "$(dirname "$0")" > /dev/null || exit 1
export SCRIPT_PATH="$(pwd)"
popd > /dev/null || exit 1

# set up color commands
if [ -t 1 ] && [ -t 2 ] && [ \( "$(type -t tput)" = "file" \) ]; then
  _colors=$(tput colors)
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

function load_git_repo() {
  INFO Cloning "$1" into "$2"
  if [[ -z "$3" ]]
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
  if [ ! -x "$(which "$1")" ]
  then
    ERR "$1 binary not found. $2"
    if [ "non-fatal" != "$3" ]
    then
      exit 1
    fi
  fi
}
