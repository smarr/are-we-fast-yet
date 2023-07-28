## This file defines common functions used for data processing.
library(stringr)
suppressMessages(library(qs))

load_data_file <- function(filename) {
  qread(filename)  
}

load_data_url <- function(url) {
  # url <- "https://rebench.stefan-marr.de/rebenchdb/get-exp-data/1518"
  safe_name <- str_replace_all(url, "[:/.]", "-")
  
  if (!file.exists("data")) {
    dir.create("data")
  }
  
  cache_file <- paste0("data/", str_replace_all(safe_name, "-+", "-"), ".qs")
  
  if(!file.exists(cache_file)) {
    download.file(url=url, destfile=cache_file)
  }
  
  tryCatch(
    qread(cache_file),
    error = function(c) {
      file.remove(cache_file)
      Sys.sleep(10)
      load_data_url(url)
    }
  )
}

load_rebench_data_file <- function(file) {
  read.csv(file,
           sep = "\t",
           header = FALSE,
           skip = 5,
           comment.char = "#",
           col.names = c("invocation", "iteration", "value", "unit",
                         "criterion", "bench", "exe", "suite", "extraargs",
                         "cores", "inputsize", "varvalue", "machine", "cmdline", "commitid", "trialid", "warmup"))
}

factorize_result <- function(result) {
  suppressWarnings({
    result$bench <- factor(result$bench)
    result$suite <- factor(result$suite)
    result$exe   <- factor(result$exe)
    result$cores <- factor(result$cores)
    
    result$varvalue  <- forcats::fct_explicit_na(factor(result$varvalue), na_level = "")
    result$inputsize <- forcats::fct_explicit_na(factor(result$inputsize), na_level = "")
    result$extraargs <- forcats::fct_explicit_na(factor(result$extraargs), na_level = "")
    
    if ("expid" %in% colnames(result)) {
      result$expid    <- factor(result$expid)
      result$trialid  <- factor(result$trialid)
      result$runid    <- factor(result$runid)
      result$commitid <- factor(result$commitid)
      
      result$cmdline <- factor(result$cmdline)
    }
    
    if ("criterion" %in% colnames(result)) {
      result$criterion <- factor(result$criterion)
      result$unit      <- factor(result$unit)
    }
    
    if ("machine" %in% colnames(result)) {
      result$machine <- factor(result$machine)
    }
  })
  
  result
}

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

## Function to calculate the needed statistics
select_data <- function(data, filter_cond) {
  filter_cond <- enquo(filter_cond)
  data %>%
    group_by(exe, bench,
             varvalue, cores, inputsize, extraargs) %>%
    select(!c(cmdline, commitid, trialid, suite, warmup, criterion)) %>%
    filter(!!filter_cond) %>%
    droplevels()
}

compute_baseline <- function(data, baseline_exe) {
  data %>%
    filter(exe == baseline_exe) %>%
    summarise(base_median = median(value),
              .groups = "drop")  %>%
    select(!exe) %>%
    droplevels()
}

compute_normalized <- function(data, baseline_data) {
  data %>%
    left_join(
      baseline_data,
      by = c("bench", "varvalue", "cores", "inputsize", "extraargs")) %>%
    group_by(
      exe, bench,
      varvalue, cores, inputsize, extraargs) %>%
    transform(ratio_median = value / base_median)
}

compute_bench_stats <- function(data) {
  data %>%
    group_by(
      exe, bench,
      varvalue, cores, inputsize, extraargs) %>%
    summarise(
      unit = unit[1],
      min = min(value),
      max = max(value),
      median = median(value),
      samples = length(value),
      
      ratio = median(value) / base_median[1],
      change_m = ratio - 1,
      .groups = "drop")
}

compute_exe_stats <- function(data) {
  data %>%
    group_by(exe) %>%
    summarise(
      unit = unit[1],
      median_ratio = median(ratio),
      min_ratio = min(ratio),
      max_ratio = max(ratio),
      .groups = "drop")
}

add_exe_median_ratio <- function (stats, stats_exe) {
  d <- stats_exe %>%
    rename(exe_ratio = median_ratio) %>%
    select(exe, exe_ratio)
  
  stats %>%
    left_join(d, by = c("exe"))
}

compute_all <- function(data, filter_cond, baseline) {
  filter_cond <- enquo(filter_cond)
  
  filtered <- data %>%
    select_data(!!filter_cond)
  
  base <- filtered %>%
    compute_baseline(baseline)
  
  norm <- filtered %>%
    compute_normalized(base)
  
  stats_bench <- norm %>%
    compute_bench_stats()
  
  stats_exe <- stats_bench %>%
    compute_exe_stats()
  
  stats_bench <- stats_bench %>%
    add_exe_median_ratio(stats_exe)
  
  list(
    filtered = filtered,
    base = base,
    normalized = norm,
    baseline = baseline,
    stats = list(
      bench = stats_bench,
      exe = stats_exe
    )
  )
}
