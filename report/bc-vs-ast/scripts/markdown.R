#!/usr/bin/env Rscript
library(rmarkdown)
args <- commandArgs(trailingOnly = TRUE)

render(args[1])
