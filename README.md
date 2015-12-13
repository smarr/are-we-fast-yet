Are We Fast Yet?
================

The goal of this project is to assess whether a language implementation is
highly optimizing and able to remove the overhead of typical abstractions used
in programs written in object-oriented languages. We are ultimately interested
in comparing language implementations with each other and optimizing them.

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


The *Core* Language
-------------------

While our initial intention is to compare object-oriented (OO) languages, the
benchmarks could be implemented on other languages as well as long as for
instance the polymorphic nature of the benchmark code can be expressed somehow.

Currently, the set of required concepts is as follows:

 - objects with fields (could be also 'records')
 - polymorphic methods on user-defined classes/objects/types
 - closures, i.e. anonymous functions with access to lexical scope (incl. changing variables)
 - basic array-like abstractions, ideally with a fixed size

Not permitted is the use of the following concepts:

 - non-local returns
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

Language Specific Guidelines
----------------------------

### Java

 - closures are realized by using arrays (this seems to be 'idiomatic')
 - should use generics for the basic collection library
 - absent features can be replaced with NotImplemented exceptions
 - symbols are realized with enums
