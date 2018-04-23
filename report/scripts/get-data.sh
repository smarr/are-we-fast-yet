#!/bin/bash
pushd `dirname $0` > /dev/null
SCRIPT_PATH=`pwd`
popd > /dev/null

mkdir -p $SCRIPT_PATH/../data/all
# scp 8:~/benchmark-results/are-we-fast-yet/latest/* data/
rsync -av 8:/home/gitlab-runner/benchmark-results/are-we-fast-yet/ $SCRIPT_PATH/../data/all/
ssh 8 'bash -s' < $SCRIPT_PATH/spec.sh >& $SCRIPT_PATH/../data/spec.md
