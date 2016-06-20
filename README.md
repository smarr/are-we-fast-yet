Are We Fast Yet? Comparing Language Implementations with Objects, Closures, and Arrays
======================================================================================

[![Build Status](https://travis-ci.org/smarr/are-we-fast-yet.svg?branch=master)](https://travis-ci.org/smarr/are-we-fast-yet)

## Goal

The goal of this project is to assess whether a language implementation is
highly optimizing and thus, is able to remove the overhead of programming
abstractions and frameworks. We are interested in comparing language
implementations with each other and optimize their compilers as well as the
run-time representation of objects, closures, and arrays.

This is in contrast to other projects such as the [Computer Language Benchmark
game][CLBG], which encourage finding the
smartest possible way to express a problem in a language to achieve best
performance.

##### Approach

To allow us to compare the degree of optimization done by the implementations
as well as the absolute performance achieved, we set the following basic rules:

  1. The benchmark is 'identical' for all languages.  
     This is achieved by relying only on a widely available and commonly used
     subset of language features and data types.

  2. The benchmarks should use language 'idiomatically'.  
     This means, they should be realized as much as possible with idiomatic
     code in each language, while relying only on the core set of abstractions.

For the detailed set of rules see [the guidelines](docs/guidelines.md) document.
For a description of the set of common language abstractions see [the *core*
language](docs/core-language.md) document.

##### Disclaimer: This is an Academic Project to Facilitate Research on Languages

To facilitate research, the goal of this project is specifically to assess the
effectiveness of compiler and runtime optimizations for a common set of common
abstractions between languages. As such, many other relevant aspects such as
GC, standard libraries, and language-specific abstractions are not included
here. However, by focusing on one aspect, we know exactly what is compared.

## Current Status

Currently, we have 14 benchmarks ported to six different languages, including
[Crystal], Java, JavaScript, Ruby, [SOM Smalltalk][SOM], and [SOMns][1] (a
[Newspeak implementation][2]).

The graph below shows the results for the
different implementations after warmup, to ensure peak performance is reported:

![Peak Performance of Java, Node.js, JRuby, JRuby+Truffle, MRI, and SOMns,
last update 2016-06-20](docs/figures/all-langs-overview-1.png?raw=true)

A detailed overview of the results is in [docs/performance.md](docs/performance.md).

The benchmarks are listed below. A detailed analysis including metrics for the
benchmarks is in [docs/metrics.md](docs/metrics.md).

#### Macro Benchmarks

 - [CD] is a simulation of an airplane collision detector. Based on
   WebKit's JavaScript [CDjs]. Originally, CD was designed to evaluated
   real-time JVMs.

 - [DeltaBlue] is a classic VM benchmark used to tune, e.g.,
   Smalltalk, Java, and JavaScript VMs. It implements a constraint
   solver.

 - [Havlak] implements a loop recognition algorithm. It has been used
   to compare C++, Java, Go, and Scala performance.

 - [Json] is a JSON string parsing benchmark derived from the
   `minimal-json` Java library.

 - [Richards] is a classic benchmark simulating an operating system
   kernel. The used code is based on [Wolczko's Smalltalk
   version][DeltaBlue].

#### Micro Benchmarks

 - Bounce simulates a ball bouncing within a box. It is based on
   a benchmark of [SOM Smalltalk][SOM].

 - List recursively creates and traverses lists. It is based on
   a benchmark of [SOM Smalltalk][SOM].

 - Mandelbrot calculates the classic fractal. It is derived from the
   [Computer Languages Benchmark Game][CLBG].

 - NBody simulates the movement of planets in the solar system. It is
   derived from the [Computer Languages Benchmark Game][CLBG].

 - Permute generates permutations of an array. It is based on a
   benchmark of [SOM Smalltalk][SOM].

 - Queens solves the eight queens problem. It is based on a benchmark
   of [SOM Smalltalk][SOM].

 - Sieve finds prime numbers based on the sieve of Eratosthenes. It is
   based on a benchmark of [SOM Smalltalk][SOM].

 - Storage creates and verifies a tree of arrays to stress the garbage
   collector. It is based on a benchmark of [SOM Smalltalk][SOM].

 - Towers solves the Towers of Hanoi game. It is based on a benchmark
   of [SOM Smalltalk][SOM].


## Contributing

Considering the large number of languages out there, we are open to
contributions of benchmark ports to new languages. We would also be interested
in new benchmarks that are in the range of 300 to 1000 lines of code.

When porting to a new language, please carefully consider [the
guidelines](docs/guidelines.md) and description of [the *core*
language](docs/core-language.md) to ensure that we can compare results.

A list of languages we would definitely be interested in is on the [issues
tracker](https://github.com/smarr/are-we-fast-yet/issues?q=is%3Aissue+is%3Aopen+label%3A%22contribution+request%22).

This includes languages like Dart, Scala, Python, and Go. Other interesting
ports could be for Racket, Clojure, or CLOS, but might require more carefully
thought out rules for porting. Similarly, ports to C++ or Rust need additional
care to account for the absence of a garbage collector.

## Getting the Code and Running Benchmarks

### Quick Start

To obtain the code, benchmarks, and documentation, checkout the git repository:

```bash
git clone --depth 1 https://github.com/smarr/are-we-fast-yet.git
```

Note that the repository relies on git submodules, which won't be loaded at that
point. They are only needed to run the full range of language implementations
and experiments.

#### Run Benchmarks for a Specific Language

The benchmarks are sorted by language in the [`benchmarks`](benchmarks) folder.
Each language has its own harness. For JavaScript and Ruby, the benchmarks are
executed like this:

```bash
cd benchmarks/JavaScript
node harness.js Richards 5 10
cd ../Ruby
ruby harness.rb Queens 5 20
```

The harness takes three parameters: benchmark name, number of iterations, and
problem size. The benchmark name corresponds to a class or file of a benchmark.
The *number of iterations* defines how often a benchmark should be executed. The
problem size can be used to influence how long a benchmark takes. Note, some
benchmarks rely on magic numbers to verify their results. Those might not be
included for all possible problem sizes.

The [rebench.conf](rebench.conf#L31) file specifies the supported problem sizes
for each benchmark.

### Using the Full Benchmark Setup

The setup and building of benchmarks and VMs is automated via
`implementations/setup.sh`. Benchmark are configured and executed with the
[ReBench](https://github.com/smarr/ReBench).

To execute the benchmarks on all supported VMs, the following implementations
are expected to be already available on the benchmark machine:

 - [Crystal](http://crystal-lang.org/docs/installation/index.html)
 - [Node.js](https://nodejs.org/en/download/)
 - [Ruby](https://www.ruby-lang.org/en/documentation/installation/)
 - GraalVM, it is expected to be available in `implementations/graalvm`.
   Please see
   [implementations/graalvm/README.md](implementations/graalvm/README.md)
   for details.

This repository uses git submodules for some of languages implementations. To
build these, additional tools are required. This includes Ant, Make, Python,
a and C/C++ compiler.

The `implementations` folder contains wrapper scripts such as `mri-23.sh`,
`java8.sh`, and `node.sh` to execute all language implementations in a common
way by ReBench.

ReBench can be installed via the Python package manager pip:

```
pip install ReBench
```

The benchmarks can be executed with the following command in the root folder:

```
rebench -d --without-nice rebench.conf all
```

The `-d` gives more output during execution, and `--without-nice` means that
the `nice` tool to enforce high process priority is not used. We don't use it
here to avoid requiring root rights.

*Note:* The [rebench.conf](rebench.conf) file specifies how and which
benchmarks to execute. It also defines the arguments to be passed to the
benchmarks.

 [1]: https://github.com/smarr/SOMns
 [2]: http://www.newspeaklanguage.org/

 [CD]:        https://www.cs.purdue.edu/sss/projects/cdx/
 [CDjs]:      https://github.com/WebKit/webkit/tree/master/PerformanceTests/JetStream/cdjs
 [DeltaBlue]: http://www.wolczko.com/java_benchmarking.html
 [Havlak]:    https://days2011.scala-lang.org/sites/days2011/files/ws3-1-Hundt.pdf
 [Json]:      https://github.com/ralfstx/minimal-json
 [Richards]:  http://www.cl.cam.ac.uk/~mr10/Bench.html
 [SOM]:       http://som-st.github.io/
 [CLBG]:      http://benchmarksgame.alioth.debian.org
 [Crystal]:   http://crystal-lang.org/
