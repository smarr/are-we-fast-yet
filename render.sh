#!/bin/sh
cd data
../scripts/knit.sh spec.md
cd ..
scripts/knit.R index.md
scripts/knit.R README.md
