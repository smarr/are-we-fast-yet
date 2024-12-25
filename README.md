Are We Fast Yet? Comparing Language Implementations with Objects, Closures, Arrays, and Strings
===================================================================================================

[![Build Status](https://github.com/smarr/are-we-fast-yet/actions/workflows/ci.yml/badge.svg)](https://github.com/smarr/are-we-fast-yet/actions/workflows/ci.yml)

## Goal

The goal of this project is to assess whether a language implementation is
*highly optimizing* and thus able to remove the overhead of programming
abstractions and frameworks. We are interested in *comparing language
implementations* (not _languages_!) with each other and optimize their compilers as well as the
run-time representation of objects, closures, arrays, and strings.

This is in contrast to other projects such as the [Computer Language Benchmark
game][CLBG], which encourage finding the
smartest possible way to express a problem in a language to achieve best
performance, an equally interesting but different problem.

#### Approach

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

The initial publication describing the project is [Cross-Language Compiler
Benchmarking: Are We Fast Yet?][3] and can be cited as ([bib file][28]):

  > Stefan Marr, Benoit Daloze, Hanspeter Mössenböck. 2016.
  > [Cross-Language Compiler Benchmarking: Are We Fast Yet?][4]
  > In Proceedings of the 12th Symposium on Dynamic Languages (DLS '16). ACM.

#### Disclaimer: This is an Academic Project to Facilitate Research on Languages

To facilitate our research, we want to be able assess the
effectiveness of compiler and runtime optimizations for a common set of
abstractions between languages. As such, many other relevant aspects such as
GC, standard libraries, and language-specific abstractions are not included
here. However, by focusing on this one aspect, we know exactly what is compared.

## Current Status

Currently, we have 14 benchmarks ported to ten different languages, including
C++, [Crystal], Java, JavaScript, Lua, Python, Ruby, [SOM Smalltalk][SOM], [SOMns][1] (a
[Newspeak implementation][2]), and Smalltalk (Squeak/Pharo).

The graph below shows some older results for
different implementations after warmup, to ensure peak performance is reported:

![Peak Performance of Java, Node.js, JRuby, JRuby+Truffle, MRI, and SOMns,
last update 2016-06-20](docs/figures/all-langs-overview-1.png?raw=true)

A detailed overview of the results is in [docs/performance.md](docs/performance.md).

The benchmarks are listed below. A detailed analysis including metrics for the
benchmarks is in [docs/metrics.md](docs/metrics.md).

#### Macro Benchmarks

 - [CD] is a simulation of an airplane collision detector. Based on
   WebKit's JavaScript [CDjs]. Originally, CD was designed to evaluate
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

Micro benchmarks are based on [SOM Smalltalk][SOM] benchmarks unless noted otherwise.

 - Bounce simulates a ball bouncing within a box.

 - List recursively creates and traverses lists.

 - Mandelbrot calculates the classic fractal. It is derived from the
   [Computer Languages Benchmark Game][CLBG].

 - NBody simulates the movement of planets in the solar system. It is
   derived from the [Computer Languages Benchmark Game][CLBG].

 - Permute generates permutations of an array.

 - Queens solves the eight queens problem.

 - Sieve finds prime numbers based on the sieve of Eratosthenes.

 - Storage creates and verifies a tree of arrays to stress the garbage
   collector.

 - Towers solves the Towers of Hanoi game.


## Contributing

Considering the large number of languages out there, we are open to
contributions of benchmark ports to new languages. We would also be interested
in new benchmarks that are in the range of 300 to 1000 lines of code.

When porting to a new language, please carefully consider [the
guidelines](docs/guidelines.md) and description of [the *core*
language](docs/core-language.md) to ensure that we can compare results.

A list of languages we would definitely be interested in is on the [issues
tracker](https://github.com/smarr/are-we-fast-yet/issues?q=is%3Aissue+is%3Aopen+label%3A%22contribution+request%22).

This includes languages like Dart, Scala, and Go. Other interesting
ports could be for Racket, Clojure, or CLOS, but might require more carefully
thought-out rules for porting. Similarly, a port to Rust need additional
care to account for the absence of a garbage collector and should be guided by our C++ port.

## Getting the Code and Running Benchmarks

### Quick Start

To obtain the code, benchmarks, and documentation, checkout the git repository:

```bash
git clone --depth 1 https://github.com/smarr/are-we-fast-yet.git
```

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
problem size can be used to influence how long a benchmark takes. Note that some
benchmarks rely on magic numbers to verify their results. Those might not be
included for all possible problem sizes.

The [rebench.conf](rebench.conf#L31) file specifies the supported problem sizes
for each benchmark.

### Using the Full Benchmark Setup

Each port of the benchmarks comes with a `build.sh` file, which either runs any
build steps needed, or with `./build.sh style` runs code style checks.
Though, the repository does not contain setup steps for the various languages anymore.
We abandoned the idea of maintaining a full setup, since it took too much work.

Benchmark are configured and executed with the
[ReBench](https://github.com/smarr/ReBench) tool.

ReBench can be installed via the Python package manager pip:

```
pip install ReBench
```

The benchmarks can be executed with the following command in the root folder,
assuming they have be previously built:

```
rebench -d --without-nice rebench.conf all
```

The `-d` gives more output during execution, and `--without-nice` means that
the `nice` tool enforcing high process priority is not used. We don't use it
here to avoid requiring root rights.

*Note:* The [rebench.conf](rebench.conf) file specifies how and which
benchmarks to execute. It also defines the arguments to be passed to the
benchmarks.

## Academic Work using this benchmark suite

- [Improving Native-Image Startup Performance](https://www.dag.inf.usi.ch/wp-content/uploads/cgo25.pdf)  
  M. Basso, A. Prokopec, A. Rosà, W. Binder.
  Proceedings of the 2025 IEEE/ACM International Symposium on Code Generation and Optimization, CGO 2025.

- [Interactive Programming for Microcontrollers by Offloading Dynamic Incremental Compilation](https://doi.org/10.1145/3679007.3685062)  
  F. Mochizuki, T. Yamazaki, S. Chiba.
  Proceedings of the 21st ACM SIGPLAN International Conference on Managed Programming Languages and Runtimes, MPLR 2024.

- [Towards Realistic Results for Instrumentation-Based Profilers for JIT-Compiled Systems](https://stefan-marr.de/downloads/mplr24-burchell-et-al-towards-realistic-results-for-instrumentation-based-profilers-for-jit-compiled-systems.pdf)  
  H. Burchell, O. Larose, S. Marr.
  Proceedings of the 21st ACM SIGPLAN International Conference on Managed Programming Languages and Runtimes, MPLR 2024.

- [Taking a Closer Look: An Outlier-Driven Approach to Compilation-Time Optimization](https://drops.dagstuhl.de/entities/document/10.4230/LIPIcs.ECOOP.2024.20)  
  F. Huemer, D. Leopoldseder, A. Prokopec, R. Mosaner, H. Mössenböck.
  Proceedings of the 38th European Conference on Object-Oriented Programming, ECOOP 2024.

- [Live Objects All The Way Down: Removing the Barriers between Applications and Virtual Machines](https://stefan-marr.de/downloads/prog23-pimas-et-al-live-objects-all-the-way-down.pdf)  
  J. E. Pimás, S. Marr, D. Garbervetsky.
  The Art, Science, and Engineering of Programming, Programming'24.

- [Don’t Trust Your Profiler: An Empirical Study on the Precision and Accuracy of Java Profilers](https://stefan-marr.de/downloads/mplr23-burchell-et-al-dont-trust-your-profiler.pdf)  
  H. Burchell, O. Larose, S. Kaleba, S. Marr.
  Proceedings of the 20th ACM SIGPLAN International Conference on Managed Programming Languages and Runtimes, MPLR'23,

- [AST vs. Bytecode: Interpreters in the Age of Meta-Compilation](https://stefan-marr.de/downloads/oopsla23-larose-et-al-ast-vs-bytecode-interpreters-in-the-age-of-meta-compilation.pdf)  
  O. Larose, S. Kaleba, H. Burchell, S. Marr.
  Proceedings of the ACM on Programming Languages, OOPSLA'23

- [Collecting Cyclic Garbage across Foreign Function Interfaces: Who Takes the Last Piece of Cake?][19]  
  T. Yamazaki, T. Nakamaru, R. Shioya, T. Ugawa, S. Chiba.
  Proceedings of the ACM on Programming Languages, PLDI 2023.

- [Simple Object Machine Implementation in a Functional Programming Language][20]  
  Filip Říha. Bachelor's Thesis, CTU Prague, 2023.

- [Supporting multi-scope and multi-level compilation in a
   meta-tracing just-in-time compiler][23]  
  Y. Izawa. PhD Dissertation. Tokyo Institute of Technology, 2023.

- [Optimizing the Order of Bytecode Handlers in Interpreters using a Genetic Algorithm][25]  
  W. Huang, S. Marr, T. Ugawa.
  The 38th ACM/SIGAPP Symposium on Applied Computing, SAC 2023.

- [Who You Gonna Call: Analyzing the Run-time Call-Site Behavior of Ruby Applications][21]  
  S. Kaleba, O. Larose, R. Jones, S. Marr.
  Proceedings of the 18th Symposium on Dynamic Languages, DLS 2022.

- [Generating Virtual Machine Code of JavaScript Engine for Embedded Systems][18]  
  Y. Hirasawa, H. Iwasaki, T. Ugawa, H. Onozawa. Journal of Information Processing. 2022.
  
- [Profile Guided Offline Optimization of Hidden Class Graphs for JavaScript VMs in Embedded Systems][24]  
  T. Ugawa, S. Marr, R. Jones.
  Proceedings of the 14th ACM SIGPLAN International Workshop on Virtual Machines and Intermediate Languages, VMIL 2022.

- [Implementation Strategies for Mutable Value Semantics][27]  
  D. Racordon, D. Shabalin, D. Zheng, D. Abrahams, B. Saeta.
  Journal of Object Technology, 2022.

- [Fusuma: Double-Ended Threaded Compaction][22]  
  H. Onozawa, T. Ugawa, H. Iwasaki. 
  Proceedings of the 2021 ACM SIGPLAN International Symposium on Memory Management, ISMM 2021.

- [A Surprisingly Simple Lua Compiler – Extended Version][17]  
  H. M. Gualandi, R. Ierusalimschy. Journal of Computer Languages. 2021.

- [Contextual Dispatch for Function Specialization][16]  
  O. Flückiger, G. Chari, M. Yee, J. Ječmen, J. Hain, J. Vitek. Proceedings of the ACM on Programming Languages, OOPSLA 2020.

- [GraalSqueak: Toward a Smalltalk-based Tooling Platform for Polyglot Programming][15]  
  F. Niephaus, T. Felgentreff, R. Hirschfeld. Proceedings of 16th International Conference on Managed Programming Languages & Runtimes, MPLR 2019.
  
- [Scopes and Frames Improve Meta-Interpreter Specialization][26]  
  V. Vergu, A. Tolmach, E. Visser. 33rd European Conference on Object-Oriented Programming, ECOOP 2019.

- [Self-Contained Development Environments][14]  
  G. Chari, J. Pimás, J. Vitek, O. Flückiger. Proceedings of the 14th ACM SIGPLAN International Symposium on Dynamic Languages. DLS 2018.
  
- [Interflow: Interprocedural Flow-Sensitive TypeInference and Method Duplication][13]  
  D. Shabalin, M. Odersky. Proceedings of the 9th ACM SIGPLAN International Symposium on Scala 2018.

- [Specializing a Meta-Interpreter: JIT Compilation of DynSem Specifications on the Graal VM][12]  
  V. Vergu, E. Visser. Proceedings of the 15th International Conference on Managed Languages and Runtimes, ManLang 2018.

- [Newspeak and Truffle: A Platform for Grace?][11]  
  S. Marr, R. Roberts, J. Noble, Grace'18, p. 3, 2018. Presentation.

- [Parallelization of Dynamic Languages: Synchronizing Built-in Collections][10]  
  B. Daloze, A. Tal, S. Marr, H. Mössenböck, E. Petrank 
  Proceedings of the ACM on Programming Languages, OOPSLA 2018

- [Efficient and Deterministic Record & Replay for Actor Languages][9]  
  D. Aumayr, S. Marr, C. Béra, E. Gonzalez Boix, H. Mössenböck 
  Proceedings of the 15th International Conference on Managed Languages and Runtimes, ManLang 2018. 

- [Fully Reflective Execution Environments: Virtual Machines for More Flexible Software][8]  
  G. Chari, D. Garbervetsky, S. Marr, S. Ducasse 
  IEEE Transactions on Software Engineering, IEEE TSE, p. 1–20, 2018.

- [Garbage Collection and Efficiency in Dynamic Metacircular Runtimes][7]  
  J. Pimás, J. Burroni, J., B. Arnaud, S. Marr 
  Proceedings of the 13th ACM SIGPLAN International Symposium on Dynamic Languages, DLS 2017.

- [Applying Optimizations for Dynamically-typed Languages to Java][6]  
  M. Grimmer, S. Marr, M. Kahlhofer, C. Wimmer, T. Würthinger, H. Mössenböck 
  Proceedings of the 14th International Conference on Managed Languages and Runtimes, ManLang 2017.

- [Efficient and Thread-Safe Objects for Dynamically-Typed Languages][5]  
  B. Daloze, S. Marr, D. Bonetta, Hanspeter Mössenböck
  In Proceedings of the 2016 ACM SIGPLAN International Conference on Object-Oriented Programming, Systems, Languages, and Applications, OOPSLA 2016.



 [1]: https://github.com/smarr/SOMns
 [2]: http://www.newspeaklanguage.org/
 [3]: https://stefan-marr.de/papers/dls-marr-et-al-cross-language-compiler-benchmarking-are-we-fast-yet/
 [4]: https://stefan-marr.de/downloads/dls16-marr-et-al-cross-language-compiler-benchmarking-are-we-fast-yet.pdf
 [5]: https://2016.splashcon.org/event/splash-2016-oopsla-efficient-and-thread-safe-objects-for-dynamically-typed-languages
 [6]: https://stefan-marr.de/downloads/manlang17-grimmer-et-al-applying-optimizations-for-dynamically-typed-languages-to-java.pdf
 [7]: https://stefan-marr.de/downloads/dls17-pimas-et-al-garbage-collection-and-efficiency-in-dynamic-metacircular-runtimes.pdf
 [8]: http://stefan-marr.de/downloads/tse18-chari-et-al-fully-reflective-execution-environments.pdf
 [9]: http://stefan-marr.de/downloads/manlang18-aumayr-et-al-efficient-and-deterministic-record-and-replay-for-actor-languages.pdf
 [10]: http://stefan-marr.de/downloads/oopsla18-daloze-et-al-parallelization-of-dynamic-languages-synchronizing-built-in-collections.pdf
 [11]: http://stefan-marr.de/downloads/grace18-marr-et-al-newspeak-and-truffle-a-platform-for-grace.pdf
 [12]: https://pure.tudelft.nl/portal/en/publications/specializing-a-metainterpreter(bc672ab9-d2bb-42d9-b67f-3f051868e3d7).html
 [13]: https://github.com/densh/talks/blob/master/2018-06-16-interflow-preprint-v1.pdf
 [14]: http://janvitek.org/pubs/dls18.pdf
 [15]: https://fniephaus.com/2019/mplr19-graalsqueak.pdf
 [16]: http://janvitek.org/pubs/oopsla20-cd.pdf
 [17]: http://www.inf.puc-rio.br/~hgualandi/papers/Gualandi-2022-SCP.pdf
 [18]: https://www.jstage.jst.go.jp/article/ipsjjip/30/0/30_679/_pdf
 [19]: https://dl.acm.org/doi/abs/10.1145/3591244
 [20]: https://dspace.cvut.cz/bitstream/handle/10467/109352/F8-BP-2023-Riha-Filip-thesis.pdf
 [21]: https://stefan-marr.de/downloads/dls22-kaleba-et-al-analyzing-the-run-time-call-site-behavior-of-ruby-applications.pdf
 [22]: https://dl.acm.org/doi/10.1145/3459898.3463903
 [23]: https://prg.is.titech.ac.jp/wp-content/uploads/2023/03/izawa-phd-dissertation.pdf
 [24]: https://stefan-marr.de/downloads/vmil22-ugawa-et-al-profile-guided-offline-optimization-of-hidden-class-graphs.pdf
 [25]: https://stefan-marr.de/downloads/acmsac23-huang-et-al-optimizing-the-order-of-bytecode-handlers-in-interpreters-using-a-genetic-algorithm.pdf
 [26]: https://drops.dagstuhl.de/opus/volltexte/2019/10796/pdf/LIPIcs-ECOOP-2019-4.pdf
 [27]: http://www.jot.fm/issues/issue_2022_02/article2.pdf
 [28]: https://github.com/smarr/are-we-fast-yet/blob/master/CITATION.bib

 [CD]:        https://www.cs.purdue.edu/sss/projects/cdx/
 [CDjs]:      https://github.com/WebKit/webkit/tree/master/PerformanceTests/JetStream/cdjs
 [DeltaBlue]: http://www.wolczko.com/java_benchmarking.html
 [Havlak]:    https://days2011.scala-lang.org/sites/days2011/files/ws3-1-Hundt.pdf
 [Json]:      https://github.com/ralfstx/minimal-json
 [Richards]:  http://www.cl.cam.ac.uk/~mr10/Bench.html
 [SOM]:       http://som-st.github.io/
 [CLBG]:      https://benchmarksgame-team.pages.debian.net/benchmarksgame/
 [Crystal]:   http://crystal-lang.org/
