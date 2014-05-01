Are we there yet? Simple Language-Implementation Techniques for the 21st Century
================================================================================

This repository contains the performance evaluation setup for the paper 
published in IEEE Software [todo-ref]. The repository and its scripts are
meant to facilitate simple re-execution of the experiments in order to reproduce
and verify the performance numbers given in the paper.

Furthermore, we use this repository to extend the performance discussion of the 
paper a little and provide additional data for the mentioned microbenchmarks.


Setup for Re-Execution of Experiments
-------------------------------------

The following steps, recreate the basic environment:

```bash
git clone --recursive -b papers/ieee-software-2014 https://github.com/smarr/selfopt-interp-performance
cd implementations
./setup.sh
```

The setup has been used and tested on Ubuntu and OS X.
The following programs are required for execution:

 - Python 2.7
 - C/C++ compiler (GCC or Clang)
 - ReBench (>= 0.5) for benchmark execution
 - knitr (>= 1.5) and R (>= 3.0) for report generation
 - Java 8 (for Graal)

Licensing
---------

The material in this repository is licensed under the terms of the MIT License.
Please note, the repository links in form of submodules to other repositories
which are licensed under different terms.
