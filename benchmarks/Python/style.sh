#!/bin/bash
pushd `dirname $0`
if [[ "$1" != "skip-black-for-python" ]]; then
  python -m black --check --diff .
fi
python -m pylint *.py som
