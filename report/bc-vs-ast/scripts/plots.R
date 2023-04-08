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


unify_overview_memory_data <- c(
"RPySOM-bc-interp" = "PySOM-bc-int",
"RPySOM-ast-interp" = "PySOM-ast-int",
"RPySOM-bc-jit" = "PySOM-bc-jit",
"RPySOM-ast-jit" = "PySOM-ast-jit",
"TruffleSOM-native-interp-bc" = "TruffleSOM-bc-NativeEE-int-main",
"TruffleSOM-native-interp-ast" = "TruffleSOM-ast-NativeEE-int-main",
"TruffleSOM-graal" = "TruffleSOM-ast-HotspotEE-jit-main",
"TruffleSOM-graal-bc" = "TruffleSOM-bc-HotspotEE-jit-main" 
)

vm_names <- c(
  "Java20-C2-jit" = "Java JDK20 C2",
  "Java17-C2-jit" = "Java JDK17 C2",
  "Java20-int"      = "Java JDK20 Int", 
  "Java-int"      = "Java JDK17 Int", 
  "Java8-C2-jit"  = "Java JDK8 C2",
  "Java8-int"     = "Java JDK8 Int", 
  "Java-native"   = "GraalVM Native EE 22.3",
  
  "CSharp-jit"    = "C# .NET 6.0.200",
 
  "Node-jit"      = "Node.js 17.9",
  "Node-int"      = "Node.js 17.9 jitless", 
  "GraalJS-HotspotEE-jit" = "Graal.js EE 22.3 Hotspot", 
  "GraalJS-NativeEE-int"  = "Graal.js EE 22.3 Native Int", 
  
  "PyPy-jit" = "PyPy 3.8 7.3.9",
  "PyPy-int" = "PyPy 3.8 7.3.9 Int",
  "CPython-int" = "CPython 3.11",
  "GraalPython-HotspotEE-jit" = "GraalPython EE 22.3 Hotspot",
  "GraalPython-NativeEE-int"  = "GraalPython EE 22.3 Native Int",
  
  "CRuby-int" = "Ruby 3.1.0",
  "CRuby-y-jit" = "Ruby 3.1.0 yjit",
  "TruffleRuby-HotspotEE-jit" = "TruffleRuby EE 22.3 Hotspot",
  "TruffleRuby-NativeEE-int"  = "TruffleRuby EE 22.3 Native Int",
  
  "PySOM-ast-jit" = "PySOM AST JIT",
  "PySOM-ast-int" = "PySOM AST Int",
  "PySOM-bc-jit"  = "PySOM BC JIT",
  "PySOM-bc-int"  = "PySOM BC Int",

  "TruffleSOM-ast-HotspotCE-jit-main" = "TruffleSOM AST Hotspot CE base", 
  "TruffleSOM-ast-HotspotEE-jit-main" = "TruffleSOM AST Hotspot EE base", 
  "TruffleSOM-ast-NativeEE-int-main"  = "TruffleSOM AST Native Int EE base",
  "TruffleSOM-ast-NativeEE-int-super" = "TruffleSOM AST Native Int EE super", 
  "TruffleSOM-ast-NativeEE-int-uber"  = "TruffleSOM AST Native Int EE uber",
  "TruffleSOM-bc-HotspotCE-jit-main"  = "TruffleSOM BC Hotspot CE base",
  "TruffleSOM-bc-HotspotEE-jit-main"  = "TruffleSOM BC Hotspot EE base",
  "TruffleSOM-bc-NativeEE-int-main"   = "TruffleSOM BC Native Int EE base",
  
  "TruffleSOM-ast-NativeCE-int-astvsbc" = "TruffleSOM AST Native Int CE",
  "TruffleSOM-bc-NativeCE-int-astvsbc" = "TruffleSOM BC Native Int CE",
  
  "SOMpp-int" = "SOM++ Int",
  "CSOM-int" = "CSOM Int",
  "SOM-RS-ast-int" = "SOM-RS AST Int",
  "SOM-RS-bc-int" = "SOM-RS BC Int",
  "ykSOM-int" = "ykSOM Int"
)

compute_color_bindings_for_plots <- function(unique_variables, viridis_fn) {
  num_variables = length(unique_variables)
  color_set = viridis_fn(num_variables)
  names(color_set) <- unique_variables
  
  return(color_set)
}

# a binding vm -> color shared for all R scripts (colorblind friendly)
color_set_vms <- compute_color_bindings_for_plots(unname(vm_names), viridis::inferno)
