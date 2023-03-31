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

vm_names <- c(
  "TruffleSOM-native-interp-bc" = "TruffleSOM BC Native CE Int", 
  "RPySOM-bc-interp" = "PySOM BC Int",
  "Java8-C2-jit"  = "Java JDK8 C2",
  "Java8-int"     = "Java JDK8 Int", 
  "Java-native"   = "GraalVM Native EE 22.3",
  "TruffleSOM-native-interp-ast" = "TruffleSOM AST Native CE Int",
  "Node-jit"      = "Node.js 17.9",
  "RPySOM-bc-jit" = "PySOM BC JIT",
  "Node-int"      = "Node.js 17.9 jitless", 
  "RPySOM-ast-interp" = "PySOM AST Int",
  "GraalJS-HotspotEE-jit" = "Graal.js EE 22.3 Hotspot", 
  "TruffleSOM-graal" = "TruffleSOM AST Hotspot CE JIT",
  "GraalJS-NativeEE-int"  = "Graal.js EE 22.3 Native Int", 
  "RPySOM-ast-jit" = "PySOM AST JIT",
  "TruffleSOM-graal-bc" = "TruffleSOM BC Hotspot CE JIT",
    "Java17-C2-jit" = "Java JDK17 C2",
  "Java-int"      = "Java JDK17 Int"
)

compute_color_bindings_for_plots <- function(unique_variables, viridis_fn) {
  num_variables = length(unique_variables)
  color_set = viridis_fn(num_variables)
  names(color_set) <- unique_variables
  
  return(color_set)
}

# a binding vm -> color shared for all R scripts (colorblind friendly)
color_set_vms <- compute_color_bindings_for_plots(unname(vm_names), viridis::inferno)
