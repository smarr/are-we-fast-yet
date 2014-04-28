#!/usr/bin/env Rscript
library(knitr);
args    <- commandArgs(trailingOnly = TRUE)
silence <- knit(args[1]);
silence <- knit2html(args[1]);

