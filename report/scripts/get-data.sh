#!/bin/bash
mkdir -p data
# scp 8:~/benchmark-results/are-we-fast-yet/latest/* data/
scp 8:/home/gitlab-runner/benchmark-results/are-we-fast-yet/ data/all
ssh 8 'bash -s' < scripts/spec.sh >& data/spec.md
