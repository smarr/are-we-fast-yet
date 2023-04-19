Cmd <- function(name, value) {
  paste0("\\newcommand{\\", name, "}{", value, "\\xspace}")
}

CmdsMedianMinMaxX <- function(cmd_prefix, data) {
  median_cmd <- paste0(cmd_prefix, "MedianX")
  min_cmd    <- paste0(cmd_prefix, "MinX")
  max_cmd    <- paste0(cmd_prefix, "MaxX")
  
  c(
    Cmd(median_cmd, X2(data$median)),
    Cmd(max_cmd,    X2(data$max)),
    Cmd(min_cmd,    X2(data$min)),
    
    Cmd(paste0(cmd_prefix, "MMMX"),
        paste0("\\", median_cmd,
               " (min.\\ \\", min_cmd,
               ", max.\\ \\", max_cmd, ")")),
    ""
  )
}

X2 <- function(num) {
  paste0(format(num, digits=2, nsmall = 2), "\\texttimes")
}

Per <- function(num) {
  paste0(format(num * 100, digits=2, nsmall = 0), "\\%")
}

WithT <- function(num) {
  format(num, big.mark = ",")
}


output_folder_path <- function (filename) {
  paste0("../../../paper/images/", filename)  
}

