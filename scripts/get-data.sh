#!/bin/bash
scp 8:Projects/ARE-WE-THERE-YET/benchmark-setup/are-we-there-yet.data data/
ssh 8 'bash -s' < scripts/spec.sh >& data/spec.md
