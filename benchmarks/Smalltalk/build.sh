#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH="$(dirname "$0")"
source "$SCRIPT_PATH/../script.inc"

if [[ "$1" = "style" ]]
then
  exit 0
elif [[ "$1" == "squeak" ]]
then
  pushd "$SCRIPT_PATH"
  if [[ ! -f "Squeak.image" ]]
  then
    ERR "Squeak.image is needed but not found."
    ERR "Please copy a Squeak.image, Squeak.changes and the corresponding *.sources file to this directory."
    exit 1
  fi
  cmd=~/.asdf/installs/awfy/squeak-6.0-22148/bin/squeak
  headless="-vm-sound-null -vm-display-null -noevents"
  if [[ ! -f "$cmd" ]]; then
    cmd=~/.asdf/installs/awfy/squeak-6.0-22148/Squeak.app/Contents/MacOS/Squeak
    headless="-headless"
  fi
  eval "$cmd" "$headless" Squeak.image build-image-squeak.st
elif [[ "$1" == "pharo" ]]
then
  pushd "$SCRIPT_PATH"
  if [[ ! -f "Pharo.image" ]]
  then
    ERR "Pharo.image is needed but not found."
    ERR "Please copy a Pharo.image, Pharo.changes and the corresponding *.sources file to this directory."
    exit 1
  fi
  ~/.asdf/installs/awfy/pharo-120/pharo Pharo.image build-image.st
else
  exit 0
fi
