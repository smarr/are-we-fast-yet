#!/bin/bash
scp 8:Projects/ZERO-OVERHEAD-MOP/zero-overhead.data.bz2 data/
ssh 8 'bash -s' < scripts/spec.sh >& data/spec.md
