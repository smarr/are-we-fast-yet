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

source("data-processing.R")
source("plots.R")
source("colors.R")
