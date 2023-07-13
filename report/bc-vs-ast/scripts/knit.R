   #!/usr/bin/env Rscript
   library(knitr);
   args    <- commandArgs(trailingOnly = TRUE)
   silence <- knit2html(args[1])