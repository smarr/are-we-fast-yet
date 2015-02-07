Zero-Overhead Metaprogramming: Reflection and Metaobject Protocols Fast and without Compromises
===================================================================================================

This repository contains the performance evaluation setup for the paper 
published at PLDI [todo-ref]. The repository and its scripts are
meant to facilitate simple re-execution of the experiments in order to reproduce
and verify the performance numbers given in the paper.

1. Setup of Experiments
-----------------------

To reexecute and verify our experiments, we provide a VirtualBox image as well
as a set of instructions to setup the experiments on another system. Note, the
additional virtualization level of VirtualBox can have an impact on the
benchmark results.

### 1.1 VirtualBox Image

The VirtualBox image contains all software dependencies, the repository with
the experiments, and the necessary compiled binaries. Thus, it allows a direct
reexecution of the experiments without additional steps.

 - download: [VirtualBox Image for Zero-Overhead Metaprogramming paper](http://TODO)
 - username: zero
 - password: zero
 - created with VirtualBox 4.3
 - the image contains a minimal Ubuntu server setup

### 1.2 Setup Instructions for other Systems

The general software requirements are as follows:

 - Python 2.7, for build tools and benchmark execution
 - C++ compiler (GCC or Clang)
 - ReBench (>= 0.7.1), for benchmark execution
 - Java 7 and 8, for Graal and Truffle
 - git, to checkout the source repositories
 - libffi headers, for RPython
 - make and maven, for the compiling the experiments
 - PyPy, to compile the RPython-based experiments
 - pip and SciPy, for the ReBench benchmarking tool


#### 1.2.1 Ubuntu

On a Ubuntu system, the following packages are required:

```bash
sudo apt-get install g++ git libffi-dev make maven \
     openjdk-7-jdk openjdk-7-source \
     openjdk-8-jdk openjdk-8-source \
     pypy python-pip python-scipy

sudo pip install ReBench
```

#### 1.2.2 Mac OS X

Required software:

1. The basic build tools are part of Xcode and are typically installed
   automatically, as soon as `gcc` or `make` is executed on the command line.
   
2. Java 7 and 8 for Truffle+Graal
  - [Java SE Development Kit 7 Downloads](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
  - [Java SE Development Kit 8 Downloads](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

The remaining software can be installed typically with either the Homebrew or 
MacPorts package manager.

Please note, to install SciPy the use of MacPorts is recommended.
However, SciPy is optional and ReBench should work without it, but we haven't
tested it.

**Homebrew (untested)**:

```bash
brew install ant libffi maven pypy brew-pip
brew pip ReBench
```
To install SciPy without MacPorts, please see the installation instructions on
[SciPy.org](http://www.scipy.org/install.html).

**MacPorts (untested)**:

```bash
sudo port install apache-ant libffi maven3 pypy py-pip py-scipy
sudo pip install ReBench
```

#### 1.2.3 Download and Compile Experiments

All experiments are part of this git repository, which uses submodules to manage the dependencies between source artifacts. When cloning the repository, ensure that the submodules are initialized properly:

```bash
git clone --recursive -b papers/zero-overhead-mop \
          https://github.com/smarr/selfopt-interp-performance
```

After all repositories have been downloaded, the experiments can be compiled as
follows. Please note that this will require further downloads. For instance the
RPython-based experiments will download the RPython sources automatically, and
the JRuby experiments will download all necessary dependencies with Maven. The
whole compilation process will take a good while. In case of errors, each part
can be started separately with the corresponding `build-$part.sh` script used
in `setup.sh`.

```bash
cd implementations
./setup.sh
```
 
2. Reexecution Instructions
---------------------------

To reexecute the benchmarks on a different system and independently verify our
measurements, either the VirtualBox image with the complete setup is necessary
or a successful built of the experiments in this repository. The built
instructions are detailed in the previous section.

To execute the benchmarks, we use the
[ReBench](https://github.com/smarr/ReBench) benchmarking tool. The experiments
and all benchmark parameters are configured in the `zero-overhead.conf` file.
The file has three main sections, `benchmark_suites`, `virtual_machines`, and
`experiments`. They describe the settings for all experiments. Each of them is
annotated with part or figure of the paper in which the results are discussed.
Note that the names used in the configuration file are post-processed for the
paper in the R scripts used to generate graphs, thus, the configuration
contains all necessary information to find the benchmark implementations in the
repositories, but does not match exactly the names in the paper.

To reexecute the experiments, ReBench is used as follows. Two important
parameter to ReBench are the `-d` switch, which shows debug output, and the
`-N` switch which disables the use of the `nice` command to increase the
process priority of the benchmarks. The `-N` is only necessary when root or sudo
are not available.

```bash
cd selfopt-interp-performance # change into the folder of this repository

# to run the benchmarks discussed in section 2.2:
sudo rebench -d zero-overhead.conf JavaReflection
sudo rebench -d zero-overhead.conf PyPyReflection

# to run the benchmarks shown in figure 4:
sudo rebench -d zero-overhead.conf OMOP-Micro

# to run the benchmarks shown in figure 5:
sudo rebench -d zero-overhead.conf OMOP-Standard

# to run the benchmarks shown in figure 6:
sudo rebench -d zero-overhead.conf JRuby

# to run the benchmarks shown in figure 7:
sudo rebench -d zero-overhead.conf Reflection
```

All benchmarks results are recorded in the `zero-overhead.data` file. The
benchmarks can be interrupted at any point and ReBench will continue the
execution where it left off. However, the results of partial runs of one
virtual machine invocation are not recorded to avoid mixing up results from
before and after the warmup phases.

3. Evaluation of Performance Results
------------------------------------

After the execution of the benchmarks, we evaluated the results using R. 
The results we measured are part of this repository and available in
`data/zero-overhead.data.bz2`. Next to the data file is the `data/spec.md` file,
which contains the basic information on the benchmark machine we used.

An annotated version of the R script used for the evaluation is given in
`evaluation.Rmd`. It can be rendered by executing `./scripts/knit.R
evaluation.Rmd`. However, this requires R and Knitr, and a variety of R
packages. We leave out the setup instructions here for brevity.

**TODO**: link to the HTML result

Licensing
---------

The material in this repository is licensed under the terms of the MIT License.
Please note, the repository links in form of submodules to other repositories
which are licensed under different terms.



