theme_simple <- function() {
    theme_bw() +
    theme(axis.text.x          = element_text(size = 12, lineheight=0.7),
          axis.title.x         = element_blank(),
          axis.title.y         = element_text(size = 12),
          axis.text.y          = element_text(size = 12),
          axis.line            = element_line(colour = "gray"),
          plot.title           = element_text(size = 12),
          panel.background     = element_blank(), #element_rect(fill = NA, colour = NA),
          panel.grid.major     = element_blank(),
          panel.grid.minor     = element_blank(),
          panel.border         = element_blank(),
          plot.background      = element_blank(), #element_rect(fill = NA, colour = NA)
          strip.background     = element_blank()) 
}
