colors <- read.table("colors.csv", sep=",", header=FALSE)

get_color <- function(hue, lightness) {
    paste0("#", colors[[lightness]][[hue + 1]])
}