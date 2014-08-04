#!/bin/sh
cd data
../scripts/knit.R spec.md
cd ..
scripts/knit.R index.Rmd
scripts/knit.R README.md
scripts/knit.R warmup.Rmd
