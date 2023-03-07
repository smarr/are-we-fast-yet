cur_dir <- getwd()
colors <- read.table(paste(cur_dir,"/colors.csv", sep=""), header=FALSE)

get_color <- function(hue, lightness) {
    paste0("#", colors[[lightness]][[hue + 1]])
}

# Color blind safe colors
## See: http://jfly.iam.u-tokyo.ac.jp/color/
## Source http://www.cookbook-r.com/Graphs/Colors_(ggplot2)/

get_safe_color_palette <- function(num_colors = 2) {
  # The palette with grey:
  cbPalette <- c("#999999", "#E69F00", "#56B4E9", "#009E73", "#F0E442", "#0072B2", "#D55E00", "#CC79A7")
  
  # The palette with black:
  cbbPalette <- c("#000000", "#E69F00", "#56B4E9", "#009E73", "#F0E442", "#0072B2", "#D55E00", "#CC79A7")
  
  if (num_colors == 2) {
    return(c(cbPalette[[2]], cbPalette[[6]]))
  }
  
  if (num_colors == 3) {
    return(c(cbPalette[[2]], cbPalette[[6]], cbPalette[[5]]))
  }
  return (cbPalette)
}