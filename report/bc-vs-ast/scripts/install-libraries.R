load_and_install_if_necessary <- function(package_name) {
  if (!suppressPackageStartupMessages(library(package_name, character.only=TRUE, logical.return=TRUE))) {
    cat(paste0("Package ", package_name, " not found. Will install it."))
    install.packages(package_name, repos='https://cloud.r-project.org/')
    library(package_name, character.only=TRUE)
  }
}

load_and_install_if_necessary("plyr")
load_and_install_if_necessary("dplyr")
load_and_install_if_necessary("ggplot2")
load_and_install_if_necessary("tables")
load_and_install_if_necessary("reshape2")
load_and_install_if_necessary("assertthat")
load_and_install_if_necessary("scales")
load_and_install_if_necessary("RColorBrewer")

load_and_install_if_necessary("forcats")

load_and_install_if_necessary("qs")
load_and_install_if_necessary("knitr")
load_and_install_if_necessary("markdown")
load_and_install_if_necessary("rmarkdown")
load_and_install_if_necessary("viridis")

load_and_install_if_necessary("gcookbook")
