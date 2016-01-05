Are We Fast Yet? Comparing Language Implementations with Objects, Closures, and Arrays
======================================================================================

The goal of this project is to assess whether a language implementation is
highly optimizing and able to remove the overhead of abstractions used in
programs written in object-oriented languages. We are ultimately interested in
comparing language implementations with each other and optimizing them.

This is in contrast to other projects such as the [Computer Language Benchmark
game](http://benchmarksgame.alioth.debian.org/), which encourage finding the
smartest possible way to express a problem in a language to achieve best
performance.

To allow us to compare the degree of optimization done by the implementations
as well as the absolute performance achieved, we set the following rules:

  1. The benchmark is 'identical' for all languages.  
     This is achieved by relying only on a widely available and commonly used
     subset of language features and data types.

  2. The benchmarks should use language 'idiomatically'.  
     This means, they should be realized as much as possible with idiomatic
     code in each language, while relying only on the core set of abstractions.

Disclaimer: This is an Academic Project to Facilitate Research on Languages
---------------------------------------------------------------------------

The main goal of the project is to have a common set of benchmarks for a wide
variety of languages that support objects, closures, and arrays. In this
setting, is is important that benchmarks can be ported easily between languages
and produce results that are comparable. One part of the portability is the
implementation effort. Therefore, all benchmarks included here are comparably
small and do not reach the size of typical applications. However, we make an
effort to avoid pure microbenchmarks in order to still capture common
programming practices and patterns that only emerge in somewhat larger
interplay of different code parts.

The *Core* Language with Objects, Closures, Arrays
--------------------------------------------------

While our initial intention is to compare object-oriented (OO) languages, the
benchmarks could be implemented on other languages as well as long as for
instance the polymorphic nature of the benchmark code can be expressed somehow.

Currently, the set of required concepts is as follows:

 - objects with fields (could be also 'records')
 - polymorphic methods on user-defined classes/objects/types
 - closures, i.e. anonymous functions with access to lexical scope (incl. changing variables)
 - basic array-like abstractions, ideally with a fixed size
 - garbage collection, currently benchmarks rely on it and we do not yet have
   variants that do manual memory management

Not permitted is the use of the following concepts:

 - non-local returns (except in `if` or to implement iteration on collections)
 - flow control in loops with continue, break, or similar abstractions
 - data structures such as hash tables, dictionaries, stacks, or queues

Idioms and good engineering advice:

 - if possible, fields should be marked as immutable, private, etc, as long as
   it does not change the language-level behavior of the benchmark (a benefit of
   consequent optimizations by the language implementation is desirable)

 - the benchmarks are supposed to be well-typed in typed languages
 
Why Is the *Core* Language Relevant?
------------------------------------

What we call the *core* language is a minimal subset of language constructs
that is widely supported also between languages. We assume that this set of
core constructs is also widely used by programmers, because it represents the
very basics. Of course, the core language excludes many concepts that also
deserve attention when optimizing. However, if this common set of concepts is
optimized in a language, novice and polyglot programmers will be able to rely
on the basic abstractions common to many languages without leaving the *sweet
spot* for fast programs.

We are generally not interested in the most efficient hash table, vector, or
random number generator. Instead, we use a common library implemented within
the *core* language subset of each language. We do this, because we want to
compare the effectiveness of the compilers instead of well optimized standard
libraries. Optimization of standard libraries could be a topic for another
benchmarking game.

Current Results
---------------

As of January 5th, 2016, we got 12 different benchmarks implemented as
identical as possible on Java, JavaScript, Ruby, and [SOMns][1] (a
[Newspeak][2] implementation). The graph below shows the results for the
different implementations after warmup, to ensure peak performance is reported:

![Peak Performance of Java, Node.js, JRuby, JRuby+Truffle, MRI, and SOMns](report/latest-results.png?raw=true)


Executing Benchmarks
--------------------

### Direct Execution for Specific Language

The benchmarks are sorted by language in the `benchmarks` folder. For each
language, there is a separate harness that can be typically executed like this:

```
cd benchmarks/JavaScript
node harness.js Richards 5 10
cd ../Ruby
ruby2.2 harness.rb Queens 5 20
```

Thus, the harness takes as first parameter the benchmark name, corresponding to
a class or file name, and then two arguments. The first is the number of
iterations and the second specifies a problem sizes, which is used to ensure an
appropriate runtime for each benchmark.

### Executing Benchmarks with given Setup and ReBench

To provide a standardized setup and execution for the benchmarks and a standard
setting of parameters, we rely on the scripts in the `implementations` folder
and the [ReBench](https://github.com/smarr/ReBench) tool.

The script `implementations/setup.sh` compiles the languages that are
configured as source dependencies in form of git submodules. The
`implementations/config.inc` file defines paths to implementations that are
available as binary releases.

*Note*: Currently automatisms are minimal, and focus supporting local and CI
environments.

The various scripts such as `mri-22.sh`, `java8.sh`, `node.sh` etc are used as
wrappers to execute all language implementations in a common way by ReBench.

To execute the benchmarks with ReBench, it can be installed via the Python
package manager pip:

```
pip install ReBench
```

Then the benchmarks can be executed with this command in the root folder:

```
rebench -d --without-nice rebench.conf all
```

The `-d` gives more output during execution, and `--without-nice` means that
the `nice` tool to enforce high process priority is not used. We don't use it
here to avoid requiring root rights.

*Note:* The [rebench.conf](rebench.conf) file specifies how and which
benchmarks to execute. It also defines the arguments to be passed to the
benchmarks.

Pull Requests Welcome!
----------------------

There is much work left for this project. There are multiple interesting
questions. On the one hand, it needs to be figured out which benchmarks to
include and which new ones to add. On the other hand, it would be interesting to
see how this approach translates to other languages.

So, below, is a list of potentially interesting things to include, but any
other suggestions, bug reports, improvements are very welcome too:

Potentially Interesting Projects:

  - port to other languages:
    - Python, as another object-oriented language
    - Racket, Clojure, CLOS: do the benchmarks still fit in a often-used subset?
    - C++, Rust, Go, Objective-C, Swift, ...: how do classic compilers fare on these benchmarks, and how does manual memory management impact them?
  - other benchmarks:
    - any classic medium sized Java, JavaScript, ... benchmarks that should be added?


Known Issues
------------

### Array Copying and Resizing

`Vector.append(e)` is implemented by copying the array with doubled size. A
language might have resizable arrays where this seems unnecessary. However, it
is supposed to do the copying in every language, to keep the benchmarks
identical. In dynamic languages, this could interfere with the implementation
strategy for arrays, for instance in JavaScript. Fortunately, JavaScript allows
resizing of the array via the `length` property. Resizing with a store at the
index of the desired length might cause the array's strategy to switch if an
incompatible value is used, or worse, might lead to a sparse representation.
So, this needs to be implemented carefully to get an array of the desired
length.

Guidelines
----------

### Language Independent

 - code should pass a linter, if available for the language  
   rational: gives some consistency with established rules
 
 - code should be within the 'expected performance sweet spot'  
   for instance, fields and collections should be used well typed
 
 - idiomatic is what is debuggable  
   For instance in JavaScript, using lexical scope to have private variables
   is problematic in some IDEs. This makes the use of normal JavaScript object
   properties preferable.
  
 - identical code structure is more important than 100% idiomatic code  
   While for the use of iteration constructs, we prefer the idiomatic version,
   in other cases it is usually preferable to use the same structure of methods
   and similar naming to avoid differences in method structure etc.

### Harness

- should be entry point for all executions
- dynamically selects benchmark to execute based on arguments

### Java

 - to realize closures which assign variables in their lexical scope, we use
   arrays, typically one-element arrays. This is necessary because Java's
   lambdas allow only read access to variables. The use of arrays as mutable
   boxes seems to be the 'idiomatic' workaround
 - use of lambdas and generally 'modern' Java is preferable
 - should use generics for the basic collection library
 - absent features can be replaced with NotImplemented exceptions
 - Smalltalk/Ruby symbols are realized with enums
 - getters and setters are used

### JavaScript

 - private fields are realized as normal object properties
 - JSLint is used
 - Smalltalk/Ruby symbols are represented as normal strings

### Ruby

 - RuboCop is used as linter

 [1]: https://github.com/smarr/SOMns
 [2]: http://www.newspeaklanguage.org/
