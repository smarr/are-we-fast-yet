This is a version of the "Are We Fast Yet?" benchmark suite
migrated to Oberon+.

The original source code was downloaded on 2020-08-08 from 
https://github.com/smarr/are-we-fast-yet/
commit 770c6649ed8e by 2020-04-03

The Java and Lua versions of the benchmark were used as a foundation of the Oberon+ implementation.

See https://github.com/rochus-keller/Oberon/tree/master/testcases/Are-we-fast-yet for the benchmark results.

When run under Mono 5 the benchmarks are about twice as fast as the LuaJIT implementation; the generated C99 source code runs even four times as fast as LuaJIT, nearly the same performance as the native C++ implementation of the benchmark suite. 

