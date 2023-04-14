Cmd <- function(name, value) {
  paste0("\\newcommand{\\", name, "}{", value, "\\xspace}")
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
