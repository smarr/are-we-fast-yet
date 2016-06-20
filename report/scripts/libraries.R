writeLines("[INFO] Loading Libraries")

load_and_install_if_necessary <- function(package_name) {
  if (!suppressPackageStartupMessages(library(package_name, character.only=TRUE, logical.return=TRUE))) {
    cat(paste0("Package ", package_name, " not found. Will install it."))
    install.packages(package_name)
    library(package_name, character.only=TRUE)
  }
}

load_and_install_if_necessary("plyr")
load_and_install_if_necessary("ggplot2")
load_and_install_if_necessary("psych")   # uses only geometric.mean
load_and_install_if_necessary("tables")
load_and_install_if_necessary("reshape2")
load_and_install_if_necessary("assertthat")
load_and_install_if_necessary("scales")
load_and_install_if_necessary("memoise")
load_and_install_if_necessary("RColorBrewer")
load_and_install_if_necessary("ggrepel")   # make sure labels don't overlap

source("data-processing.R")
source("plots.R")
source("colors.R")

# avoid scientific notation for numbers, it's more readable to me
options(scipen=999)

# prints stack trace on error, from: http://stackoverflow.com/a/2000757/916546
options(warn = 2, keep.source = TRUE, error = 
          quote({ 
            cat("Environment:\n", file=stderr()); 
            
            # TODO: setup option for dumping to a file (?)
            # Set `to.file` argument to write this to a file for post-mortem debugging    
            dump.frames();  # writes to last.dump
            
            #
            # Debugging in R
            #   http://www.stats.uwo.ca/faculty/murdoch/software/debuggingR/index.shtml
            #
            # Post-mortem debugging
            #   http://www.stats.uwo.ca/faculty/murdoch/software/debuggingR/pmd.shtml
            #
            # Relation functions:
            #   dump.frames
            #   recover
            # >>limitedLabels  (formatting of the dump with source/line numbers)
            #   sys.frame (and associated)
            #   traceback
            #   geterrmessage
            #
            # Output based on the debugger function definition.
            
            n <- length(last.dump)
            calls <- names(last.dump)
            cat(paste("  ", 1L:n, ": ", calls, sep = ""), sep = "\n", file=stderr())
            cat("\n", file=stderr())
            
            if (!interactive()) {
              q(status=1) # indicate error
            }
          }))

