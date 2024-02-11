The *Core* Language with Objects, Closures, Arrays, and Strings
===============================================================

The goal of the project is to have a common set of benchmarks for a wide variety
of languages that support objects, closures, arrays, and strings.
It is important that benchmarks can be ported easily between languages and
produce comparable results. To ensure portability, the benchmarks use only a
set of language abstractions that is common to a wide range of languages.


## Why Is the *Core* Language Relevant?

What we call the *core* language is a small subset of language constructs
that is widely supported in different languages. We assume that this set of
core constructs is also widely used by programmers, because it represents the
very basics. Of course, the core language excludes many concepts that also
deserve attention when optimizing. However, if this common set of concepts is
optimized in a language, novice and polyglot programmers will be able to rely
on the basic abstractions common to many languages without leaving the *sweet
spot* for fast programs. As such, we see an optimal implementation of the *core*
language as a necessary condition to achieve optimal application performance.
However, depending on a specific language and its features it is by far not
sufficient to optimize only the *core* language to achieve optimal performance.

We are generally not interested in the most efficient hash table, vector, or
random number generator. Instead, we use a common library implemented within
the *core* language subset of each language. We do this because we want to
compare the effectiveness of the compilers instead of well optimized standard
libraries. Optimization of standard libraries could be a topic for another
benchmarking game. Note, this common library typically implements basic
iteration with some of the control flow features that are generally not
permitted.

## The *Core* Languages

While our intention is to compare object-oriented (OO) languages, the
benchmarks could be implemented on other languages as well as long as for
instance the polymorphic nature of the benchmark code can be expressed.

#### Required Abstractions

The set of required concepts is:
  - objects with fields, could be also records or structs
  - polymorphic methods on user-defined classes/objects/types
  - closures, i.e. anonymous functions with read and write access to their
    lexical scope
  - basic array-like abstractions, ideally with a fixed size
  - strings, with access to individual characters,
    support for mutation is not required

For some languages, a mapping of these abstraction is not trivial and we define
[guidelines](guidelines.md) for these cases. For example, Java does not support
writing to variables in the outer scope of a closure. Therefore, we follow the
common practice to use an array as a workaround.

#### Excluded Abstractions

To guarantee comparable results, deterministic execution, and identical
work-load across languages, we do not allow the use of the following
concepts:

  - built-in data structures such as hash tables, dictionaries, stacks,
    or queues
  - object-identity-based hash
  - non-local returns (except in `if` or to implement iteration on collections)
  - flow control in loops with `continue`, `break`, or similar abstractions
    except to implement iterator functions on collections
  - single character abstractions, such as `char` in Java or C,
    since they are not supported by all languages and would change the challange
    for the compiler

These abstractions can cause behavior that is not comparable between languages
or language implementations. For example, the object-identity-based hash often
uses memory addresses leading to non-deterministic distribution of objects in
hash buckets. Similarly, built-in collections can use different algorithms,
which would mean we benchmark these algorithms instead of the compiler and
runtime system we are interested in.
