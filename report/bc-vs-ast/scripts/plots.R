theme_simple <- function(font_size = 6) {
    theme_bw() +
    theme(axis.text.x          = element_text(size = font_size, lineheight=0.7),
          axis.title.x         = element_blank(),
          axis.title.y         = element_text(size = font_size),
          axis.text.y          = element_text(size = font_size),
          axis.line            = element_line(colour = "gray"),
          plot.title           = element_text(size = font_size),
          legend.text          = element_text(size = font_size),
          legend.background    = element_blank(),
          legend.box.background = element_blank(),
          legend.key           = element_blank(),
          panel.background     = element_blank(), #element_rect(fill = NA, colour = NA),
          panel.grid.major     = element_blank(),
          panel.grid.minor     = element_blank(),
          panel.border         = element_blank(),
          plot.background      = element_blank(), #element_rect(fill = NA, colour = NA)
          strip.background     = element_blank(),
          strip.text           = element_text(size = font_size),
          plot.margin = unit(c(0,0,0,0), "cm")) 
}

theme_simple_axis_title <- function(font_size = 6) {
  element_text(size = font_size, family="Arial")
}

theme_simple_font_size <- function() {
  6
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
  "Java20-C2-jit" = "Java", # JDK20 C2",
  "Java17-C2-jit" = "Java", # JDK17 C2",
  "Java20-int"    = "Java", # "JDK~20~`-Xint`", 
  "Java-int"      = "JDK17", # -Xint", 
  "Java8-C2-jit"  = "Java JDK8 C2",
  "Java8-int"     = "Java JDK8 Int", 
  "Java-native"   = "GraalVM Native EE 22.3",
  
  "CSharp-jit"    = "C# .NET 6.0.200",
 
  "Node-jit"      = "Node.js",# 17.9",
  "Node-int"      = "Node.js", # "Node.js~17.9~`--jitless`", 
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
  
  "PySOM-ast-jit" = "PySOM[AST]",
  "PySOM-ast-int" = "PySOM[AST]",
  "PySOM-bc-jit"  = "PySOM[BC]", # BC JIT",
  "PySOM-bc-int"  = "PySOM[BC]", # BC Int",
  
  "RPySOM-ast-jit" = "PySOM[AST]",
  "RPySOM-bc-jit"  = "PySOM[BC]",

  "TruffleSOM-ast-HotspotCE-jit-main" = "TSOM[AST]", # AST Hotspot CE base", 
  "TruffleSOM-ast-HotspotEE-jit-main" = "TSOM[AST]", # AST Hotspot EE base", 
  "TruffleSOM-ast-NativeEE-int-main"  = "TSOM[AST]", # AST Native Int EE base",
  "TruffleSOM-ast-NativeEE-int-super" = "TSOM[AST]", # AST Native Int EE super", 
  "TruffleSOM-ast-NativeEE-int-uber"  = "TSOM[AST]", # AST Native Int EE uber",
  "TruffleSOM-bc-HotspotCE-jit-main"  = "TSOM[BC]", # BC Hotspot CE base",
  "TruffleSOM-bc-HotspotEE-jit-main"  = "TSOM[BC]", # BC Hotspot EE base",
  "TruffleSOM-bc-NativeEE-int-main"   = "TSOM[BC]", # BC Native Int EE base",
  
  "TruffleSOM-ast-NativeCE-int-astvsbc" = "TSOM[AST]", # AST Native Int CE",
  "TruffleSOM-bc-NativeCE-int-astvsbc" = "TSOM[BC]", # BC Native Int CE",
  
  "TruffleSOM-graal-ast" = "TSOM[AST]",
  "TruffleSOM-graal-bc" = "TSOM[BC]",
  "TruffleSOM-native-ast" = "TSOM[AST]",
  "TruffleSOM-native-bc" = "TSOM[BC]",
  
  "SOMpp-int" = "SOM++ Int",
  "CSOM-int" = "CSOM Int",
  "SOM-RS-ast-int" = "SOM-RS AST Int",
  "SOM-RS-bc-int" = "SOM-RS BC Int",
  "ykSOM-int" = "ykSOM Int"
)

set_color_bindings_for_plots <- function(unique_variables, color_set_opti_vm) {
  color_set = color_set_opti_vm
  names(color_set) <- unique_variables
  
  return(color_set)
}

sublist_vms_jit <- c("PySOM-ast-jit", "PySOM-bc-jit", 
                    "TruffleSOM-ast-HotspotCE-jit-main",
                    "TruffleSOM-bc-HotspotCE-jit-main", "Java20-C2-jit", "Node-jit")
                
sublist_vms_int <- c("PySOM-ast-int", "PySOM-bc-int",
                    "TruffleSOM-ast-NativeCE-int-astvsbc", "TruffleSOM-bc-NativeCE-int-astvsbc",
                    "TruffleSOM-native-interp-bc-supernodes",
                    "Java20-int", "Node-int")

color_palette_vm  = c("#FF6E3A", "#00C2F9","#009F81",  "#FF5AAF","#00FCCF","#9F0162",   "#8400CD", "#008DF9",  "#FFB2FD", "#E20134", "#A40122","#FFC33B", "#000000" )#from http://mkweb.bcgsc.ca/colorblind/palettes/12.color.blindness.palette.txt

color_set_vms <- set_color_bindings_for_plots(unique(revalue(c(sublist_vms_int,sublist_vms_jit), vm_names)), color_palette_vm)
