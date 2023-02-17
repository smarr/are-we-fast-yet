theme_simple <- function(font_size = 12) {
    theme_bw() +
    theme(axis.text.x          = element_text(size = font_size, lineheight=0.7),
          axis.title.x         = element_blank(),
          axis.title.y         = element_text(size = font_size),
          axis.text.y          = element_text(size = font_size),
          axis.line            = element_line(colour = "gray"),
          plot.title           = element_text(size = font_size),
          legend.text          = element_text(size = font_size),
          panel.background     = element_blank(), #element_rect(fill = NA, colour = NA),
          panel.grid.major     = element_blank(),
          panel.grid.minor     = element_blank(),
          panel.border         = element_blank(),
          plot.background      = element_blank(), #element_rect(fill = NA, colour = NA)
          strip.background     = element_blank(),
          strip.text           = element_text(size = font_size),
          plot.margin = unit(c(0,0,0,0), "cm")) 
}

element90 <- function() { element_text(angle = 90, hjust = 1, vjust=0.5) }
