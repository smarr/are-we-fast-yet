#!/bin/bash
scp 8:Projects/ZERO-OVERHEAD-MOP/zero-overhead.data data/
ssh 8 'bash -s' < scripts/spec.sh >& data/spec.md
