#!/usr/bin/env Rscript
library(knitr);
args    <- commandArgs(trailingOnly = TRUE)
#silence <- knit(args[1]);
silence <- knit2html(args[1],
                     header = "<link href='http://fonts.googleapis.com/css?family=Lora:400,400italic|Montserrat:400,700' rel='stylesheet' type='text/css'>",
                     styleshee = '
                     body {
                        padding-top:  1em;
                        max-width:   40em;
                        margin:      auto;
                     }
                     p { 
                         font-family: Lora,"Times New Roman",serif;
                         font-weight: 400;
                         font-size: 14pt;
                         text-align: justify;
                     }
                     
                     td {
                         font-family: "Lucida Console", Monaco, monospace;
                         font-weight: 300;
                         font-size:80%
                     }
                     
                     .center, .center { text-align: center; }
                     .right,  .right  { text-align: right; }
                     .left,   .left   { text-align: left; }
                     
                     h1, h2, h3, h4, h5 {
                         font-family: Montserrat,Helvetica,Arial,sans-serif;
                         font-weight: bold;
                     }
                     
                     th {
                         font-family: Montserrat,Helvetica,Arial,sans-serif;
                         font-weight: 400;
                     }
                     
                     img {
                         margin: auto;
                         display: block;
                     }
                     
                     .half table { width: 50%; }
                     .center table { margin: auto; }
                     .full table { width: 100% }
                     ');

