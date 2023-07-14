#!/usr/bin/env Rscript
args    <- commandArgs(trailingOnly = TRUE)
silence <- rmarkdown::render(args[1])
