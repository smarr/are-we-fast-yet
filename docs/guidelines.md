Guidelines
==========

To ensure that these benchmarks assess compiler effectiveness, we provide a set
of guidelines and general requirements for their implementations and ports to
different languages.

## Requirements for Benchmark Implementations

##### As Identical as Possible.

The first and foremost requirement is that benchmarks must be as *identical* as
possible between languages. This is achieved by relying only on a commonly used
subset of language features and data types. These language features also need to
be used consistently between languages. For example, to iterate over arrays, a
benchmark should use a `forEach()` method that takes a closure, or should fall
back to a `for-each` loop construct if available in the language. We prefer the
use of a `forEach()` method, because it is commonly supported, is a high-level
abstraction, and exercises the compiler's abilities to optimize closures.
Another example is the semantics of arrays. Languages such as Java and Smalltalk
offer only fixed-sized arrays, while JavaScript and Ruby have dynamically-sized
arrays. To ensure that the executed operations are as identical as possible, we
use arrays as fixed-sized entities in all languages. This ensures that we
benchmark the same operations, and expose all compilers to the same challenges.

##### Idiomatic Use of Language.

To stay within the performance sweet spot of language implementations, the
second most important requirement is to use a language as *idiomatically* as
possible, i.e., following best practices. As a guideline for idiomatic language
usage, we rely on widely used lint and code style checking tools for the
languages. In some cases this might be a tradeoff with the requirement to be as
identical as possible. We prefer here the more idiomatic solution if the result
is close enough in terms of use of control structures, closures, and other
performance-related aspects, and stays within the *core* language. Simple
idiomatic differences include that a comparison in Java `if (node != null)` is
written simply as `if node` in Ruby.

This also includes structural information not present in all languages, e.g.,
types and information about immutability. Thus, in languages such as Java, we
use the `private` and `final` keywords on fields and methods to provide hints to
the compiler.  We consider this idiomatic and desirable language use to enable
optimizations.

##### Well-typed Behavior.

The benchmarks are designed to be well-typed in statically-typed languages, and
behave well-typed in dynamic languages to ensure portability and identical
behavior. For dynamic languages this means that initialization of fields and
variables avoids suppressing possible optimizations. For example, fields that
contains primitive values such as integers or doubles are not initialized with
pointer values such as `null` but with the appropriate `0`-value to ensure that
the implementation can optimize these fields.

##### Fully Deterministic and Identical Behavior.

The benchmarks are designed to be fully deterministic and show identical
behavior in the benchmark's code on all languages. This means, repeated
executions of the benchmark take the same path through the benchmark code. On
the one hand, this is necessary to ensure reproducible results, and on the other
hand, this is required to ensure that each language performs the same amount of
work in a benchmark. However, differences of language semantics and standard
libraries can lead to differences in the absolute amount of work that is
performed by a benchmark.

## Guidelines for New Languages

When porting benchmarks to a new language, many design decisions need to be made
that potentially tradeoff idiomatic language usage with the comparability
requirements of the benchmarks.

As a basic guideline, we use the following rules:

 - Code should pass a linter (if available). This gives some basic consistency
   with established rules. However, some language-specific rules impact
   comparability for an example see our [Ruby
   Rubocop](../benchmarks/Ruby/.rubocop.yml) settings.

 - Code should be within the 'expected performance sweet spot' of a language.
   For instance, fields and collections need to be used well-typed.

 - Idiomatic is what is debuggable.
   For instance in JavaScript, using lexical scopes for private variables
   is problematic in some IDEs. This makes the use of normal JavaScript object
   properties preferable.

 - Identical code structure is more important than 100% idiomatic code.
   While prefer the use of idiomatic iteration constructs, in other cases it is
   required to use the same structure of methods and similar naming to
   prevent differences in the general code structure etc.

 - Use available language constructs to indicate immutability, or visibility of
   methods, fields, and other constructs. As long as it does not change the
   behavior of a benchmark, it is desirable to benefit from potential
   optimizations.

## Example Choices for Currently Supported Languages

As a guide for new languages, we list a few of our choices for existing
languages.

### Harness

 - should be entry point for all executions
 - dynamically selects benchmark to execute based on arguments

### Java

 - To realize closures which assign variables in their lexical scope, we use
   arrays, typically one-element arrays. This is necessary because Java's
   lambdas allow only read access to variables. The use of arrays as mutable
   boxes seems to be the idiomatic workaround.
 - We use lambdas and generally *modern* Java.
 - The code collection library uses generics.
 - Absent features are replaced by `NotImplemented` exceptions.
 - Smalltalk/Ruby symbols are realized with enums.
 - The code uses getters and setters instead of plain field accesses.
 - We use Checkstyle as a linter.

### JavaScript

 - Private fields are realized as normal object properties.
 - Smalltalk/Ruby symbols are represented as normal strings.
 - We use `Array` as if it is fixed sized. To increase the size of an array,
   we copy it with `.slice()` and then set the `length` property. This way, the
   work done by the different languages as as identical as possible.
 - We use JSHint as a linter.

### Ruby

 - We use RuboCop as a linter.
 - We use `if var` to check for non-null instead of the less idiomatic `if !var.nil?`.

### Lua

 - We write code compatible with Lua 5.1, 5.2 and 5.3.
 - Smalltalk/Ruby symbols are represented as normal strings.
 - We use Lua 1-based array and the length operator #.
 - We use single object when a class is not required.
 - Bitwise operators with various Lua versions is a nightmare.
 - We use luacheck as a linter.

## Repository Structure

 - `[benchmarks](../benchmarks)` contains for each language one folder with the
   benchmark implementations
 - `[docs](../docs)` contains the documentation for this project
 - `[implementations](../implementations)` contains the language implementations
   in some cases as git submodules. It also contains the build and launcher
   scripts.
 - `[report](../report)` contains R scripts to process benchmark results.
 - `[rebench.conf](../rebench.conf)` contains the benchmark settings as a
   ReBench configuration.
