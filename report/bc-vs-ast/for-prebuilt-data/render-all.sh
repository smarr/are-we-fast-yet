#!/bin/sh
../scripts/knit.R individual-optimisations.Rmd
../scripts/knit.R memory_usage_analysis.Rmd
../scripts/knit.R overview.Rmd
../scripts/knit.R smarr-mem-use.Rmd
../scripts/knit.R warmup.Rmd
