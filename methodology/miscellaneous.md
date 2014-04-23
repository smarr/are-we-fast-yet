Miscellaneous 
=============

This report uses the [Extended Tango color palette](http://emilis.info/other/extended_tango/).


```r
colors <- read.table("../scripts/colors.csv", sep = ",", header = FALSE)
plot(0, type = "n", ylab = "", xlab = "", axes = FALSE, ylim = c(nrow(colors) + 
    1, 0), xlim = c(0, ncol(colors) + 1))

for (i in 1:(ncol(colors))) {
    for (j in 1:(nrow(colors))) {
        val <- colors[[i]][[j]]
        if (i == 1) {
            bgCol <- "#ffffff"
            txt <- val
        } else {
            bgCol <- paste0("#", val)
            txt <- bgCol
        }
        
        rect(i - 0.5, j - 0.5, i + 0.5, j + 0.5, border = "black", col = bgCol)
        text(i, j, txt, col = "#000000")
    }
}
```

![plot of chunk unnamed-chunk-1](figure/unnamed-chunk-1.png) 


