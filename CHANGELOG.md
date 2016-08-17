# Change Log

## [Unreleased]

## [v1.1] - 2016-08-17

 - Published paper:
     > Stefan Marr, Benoit Daloze, Hanspeter Mössenböck. 2016.
     > [Cross-Language Compiler Benchmarking: Are We Fast Yet?][1]
     > In Proceedings of the 12th Symposium on Dynamic Languages (DLS '16). ACM.
 
 - Added Smalltalk version of the benchmarks.  
   Implementation uses a SOM parser to simplify maintenance of benchmarks.
   Benchmarks are tested with Pharo 5.0, but should be compatible with Squeak,
   too.
 - fixed some style issues for Ruby benchmarks for latest rubocop
 - fixed Java Checkstyle URL
 - fixed Dictionary size tracking (not benchmark relevant)
 - fixed JavaScript harness so that total is always an integer

## [v1.0] - 2016-06-22

First release of Are We Fast Yet. Documented with [README.md] and [docs]. The
release contains all changes since the [initial][v1.0] addition of benchmarks
to the repository.

[Unreleased]: https://github.com/smarr/are-we-fast-yet/compare/v1.1...HEAD
[v1.1]:       https://github.com/smarr/are-we-fast-yet/compare/v1.0...v1.1
[v1.0]:       https://github.com/smarr/are-we-fast-yet/compare/3dfee54...v1.0
[README.md]:  https://github.com/smarr/are-we-fast-yet#readme
[docs]:       https://github.com/smarr/are-we-fast-yet/tree/master/docs
[1]: http://stefan-marr.de/papers/dls-marr-et-al-cross-language-compiler-benchmarking-are-we-fast-yet/
