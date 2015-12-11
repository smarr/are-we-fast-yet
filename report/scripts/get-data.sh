#!/bin/bash
scp 8:Projects/selfopt-interp-performance/performance-overview/perf-overview.data data/
ssh 8 'bash -s' < scripts/spec.sh >& data/spec.md
