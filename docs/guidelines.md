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

 - Code should pass a linter, if available, to ensure some basic consistency
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
   While we prefer idiomatic iteration constructs, in other cases it is
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

### Python

 - Use plain fields instead of getter/setter when they would be trivial


### C++

With C++, we added support for the first language that does not have garbage
collection, which comes with a new dimension of issues to be considered.

Explicit Memory Management Rules:

 - benchmarks have to run without memory leaks

 - stack allocation can be used where natural and where it does not change the
   nature of the benchmark, for instance when an object/array is used only in
   the dynamic scope of a function

 - existing data structures should be used where possible to manage
   dynamically-created objects, for instance, iterate over an already existing
   list of objects at the end of the benchmark or a method to free them

 - changes to code structure and APIs should be as minimal as possible,
   for instance, if a method returns an allocated object, leave it as such,
   and let the caller manage the memory

 - if the useful lifetime of object fields is restricted to a method,
   the allocations referenced by these fields should be freed before
   the end of a method, but the field should remain a field.

 - for arbitrary object graphs, as in DeltaBlue, `memory/object_tracker.h` can
   be used to free the objects when not needed.
   The use of `shared_ptr` may also be appropriate, but did not work for DeltaBlue.

Memory Management Strategies Per Benchmarks:

 - **CD** use value objects for most data. Since it's a tree, the red/black tree
   is trivially managed by deleting the nodes from the root. Vectors are managed
   explicitly for the voxel map. Don't miss the empty vectors that are not
   passed on as result though.

 - **DeltaBlue** uses `object_tracker`, since there are cyclic dependencies,
   but we can free the full setup once it's not needed.
   A mix of `shared_ptr` and `weak_ptr` could probably also work.

 - **Havlak** manages memory explicitly by assigning ownership to specific
   classes. Specifically, the ControlFlowGraph owns the basic blocks and
   block edges, the LoopStructureGraph owns the loops, the HavlakLoopFinder
   owns its data including UnionFindNodes. Thus, the destructors can free
   the corresponding memory.

 - **Json** relies on JSON documents being trees, and uses the
   tree to free objects. The major tradeoff here is that we need to allocate
   `true`, `false`, and `null` literal objects to have a uniform memory representation.
   Though, otherwise, we do not require any management overhead.

 - **Richards** uses `object_tracker` for simplicity.
   It could use `shared_ptr` and when accounting for cyclic references that
   could work, too. Naively freing the task list did not seem to work,
   but I might have missed something.

 - **Bounce** allocates everything statically, i.e., on the stack.

 - **List** trivially uses the list structure for freeing the list.

 - **Mandelbrot** does not allocate any data structures.

 - **NBody** allocates everything statically, i.e., on the stack.

 - **Permute** allocates an array dynamically, and frees it directly.
   Since the benchmark holds the reference in a field, and allocates on
   each iteration, the new/delete dance is needed to comply.

 - **Queens** allocates its arrays dynamically, and frees them directly,
   same as Permute.

 - **Sieve** allocates everything statically, i.e., on the stack.

 - **Storage** allocates its tree dynamically, and frees it from the root.

 - **Towers** allocates the disks dynamically, which form a linked list,
   that is used to free them once not needed anymore.

General C++-isms:

 - the benchmarks, where possible, can be in headers only to match the code
   structure of other languages

 - we use clang-tidy and clang-format

 - use `std::array` for fixed-sized arrays

 - use `const` where it is appropriate, but it won't really work with containers
   and can be problematic for value classes

 - use `auto` and `auto*` to make code more concise as recommended by linter,
   for instance for allocations

 - use annotations like `[[nodiscard]]` where indicated by the linter

 - use modern C++-isms, for instance range loops and `.at()` instead of `[]` on `std::array`

 - use initializer syntax for default values and member initializers lists when depending on constructor parameter

 - prefer `int32_t`, `size_t` and similar to be more explicit about semantics
   and size of value, plain `int`/`long` shouldn't be used

 - avoid changing signatures for the sake of the compiler. It should do an
   appropriate return-value optimization itself.

 - use templates where Java uses generics


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
