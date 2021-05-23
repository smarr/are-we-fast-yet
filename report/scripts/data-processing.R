## This file defines common functions used for data processing.

load_all_data <- function (folder, data_file_prefix = "") {
  ## folder <- "data"
  ## data_file_prefix <- "AWFY-"
  
  result <- NULL
  files <- sort(list.files(folder, paste0(data_file_prefix, "[0-9]+")))
  
  for (f in files) {
    # f <- files[[1]]
    data <- load_data_file(paste(folder, f, sep = "/"))
    result <- rbind(result, data)
  }
  
  result
}

load_data_file <- function(filename) {
  qread(filename)  
}

prepare_vm_names <- function(data) {
  name_map <-     list("Java8U66"              = "Java, 1.8.0_66",
                       "JRubyJ8"               = "Ruby, JRuby 9.0.4.0 + indy", #invokedynamic
                       "MRI22"                 = "Ruby, MRI 2.2",
                       "MRI23"                 = "Ruby, MRI 2.3",
                       "RBX314"                = "Ruby, Rubinius 3.14",
                       "GraalJS"               = "JavaScript, GraalJS",
                       "Node"                  = "JavaScript, Node.js 5.4.0",
                       "SOMns"                 = "SOMns, Newspeak, master",
                       "SOMns-Enterprise"      = "SOMns, Newspeak",
                       "JRubyTruffle"          = "Ruby, JRuby+Truffle, truffle head (basic)",
                       "JRubyTruffleEnterprise" = "Ruby, JRuby+Truffle", # , truffle head
                       
                       "TruffleSOM-graal"      = "TruffleSOM",
                       "TruffleSOM-graal-no-split" = "TruffleSOM.ns",
                       "SOMpp"                 = "SOM++")
  # Rename
  levels(data$VM)  <- map_names(
    levels(data$VM),
    name_map)
  data
}

map_names <- function(old_names, name_map) {
  for (i in 1:length(old_names)) {
    old_name <- old_names[[i]]
    if (!is.null(name_map[[old_name]])) {
      old_names[i] <- name_map[[old_name]]
    }
  }
  old_names
}
