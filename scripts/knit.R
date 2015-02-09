#!/usr/bin/env Rscript
library(knitr);
args    <- commandArgs(trailingOnly = TRUE)
#silence <- knit(args[1]);
silence <- knit2html(args[1],
                     header = "<link href='http://fonts.googleapis.com/css?family=Lora:400,400italic%7CMontserrat:400,700' rel='stylesheet' type='text/css'>
    <script>
      (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
      (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
      m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
      })(window,document,'script','//www.google-analytics.com/analytics.js','ga');
      ga('create', 'UA-8217459-1', 'auto');
      ga('send', 'pageview');
    </script>",
                     styleshee = 'body {
        padding-top:  1em;
        max-width:   40em;
        margin:      auto;
     }
     p, .author, figcaption, li, a, div, td { 
        font-family: Lora,"Times New Roman",serif;
        font-weight: 400;
        font-size: 14pt;
     }
     
     p {text-align: justify;}
                     
     .center, .center, header, header > div, figcaption, th { text-align: center; }
     .right,  .right  { text-align: right; }
     .left,   .left   { text-align: left; }
                     
     h1, h2, h3, h4, h5 {
        font-family: Montserrat,Helvetica,Arial,sans-serif;
        font-weight: bold;
     }
     
     h1 span { font-size: 80% }
                     
     th, figcaption {
        font-family: Montserrat,Helvetica,Arial,sans-serif;
        font-weight: 400;
     }
     figure figure figcaption, .tab-separator th {
       font-weight: 300;
       font-family: Helvetica,Arial,sans-serif;
     }
     .tab-separator th {font-style: italic; text-align:left; padding-top:0.5em;}
     figure {
       text-align: center;
     }
     figure figure {
       display: inline-block;
       margin: auto;
       vertical-align: top;
     }
     figure figure img {
       width: 100%;
     }
                     
     img {
        margin: auto;
        display: block;
     }
                     
     .half table { width: 50%; }
     .center table { margin: auto; }
     .full table { width: 100% }
     
     /* code */
     pre .comment, .comment, .pl-c {color:#4e9a06;}
     pre .string, pre .literal, pre .number, .number, .pl-c1, .pl-s1  {color:#5c3566;}
     pre .identifier, .arg, .pl-vpf     {font-style:italic;}
     .self, pre .operator {color:#204a87;}
     pre .keyword, .keyword, .pl-s3, .pl-k {font-weight:bold; color: inherit;}

     .som, code, pre, .linenumbers {
       font-family: "Lucida Console", Monaco, monospace;
       font-weight: 300;
       font-size:   90%;
       text-align:  left;
     }
     .linenumbers {
       float: left;
       width: 1.3em;
       text-align: right;
       padding-right: 1em;
       color: #cccccc;
       font-style: normal;
     }
     
     table {border-spacing: 0px;
            border-collapse: separate;}
     .hline-top td, .hline-top th {border-top: 1px solid black;}
     .hline-bottom td, .hline-bottom th {border-bottom: 1px solid black;}
     td {font-size: 11pt; text-align: left; padding:0.2em;}

     #references li {padding-bottom:0.4em;}
     
     a:link { color: #000099; font-size: inherit;}
     a:visited { color: #330077; font-size: inherit;}
     
     header div {padding-top:0.4em;}
     header h1 {font-size:2em;}
     
     .sidenote {position: absolute;
                padding-left: 44em;
                width: 10em;
                font-size: 80%;
                text-align: left;
                display: block;}
      .math {font-family: Georgia, serif;
          font-style: italic}
      .math .txt {font-style: normal;}
      pre {background-color: #eee; padding: 10px;}
      pre code, pre {font-size: 11pt; white-space: pre-wrap; }
                     ');

