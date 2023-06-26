This is a version of the "Are We Fast Yet?" benchmark suite
migrated to C++98.

The original source code was downloaded on 2020-08-08 from 
https://github.com/smarr/are-we-fast-yet/
commit 770c6649ed8e by 2020-04-03

The implementation was directly derived from the Java implementation from https://github.com/smarr/are-we-fast-yet/tree/master/benchmarks/Java.

Benchmark results can be found in https://github.com/rochus-keller/Oberon/tree/master/testcases/Are-we-fast-yet, see Are-we-fast-yet_results.ods and Are-we-fast-yet_results_linux.pdf.

As expected, the C++ implementation runs the fastest of all the ones I have measured. The second fastest is the implementation in Crystal and the C99 implementation generated from the Oberon+ version; both are only 20% slower than the C++ version, i.e. almost equally fast. 
