Zero-Overhead Metaprogramming: Reflection and Metaobject Protocols Fast and without Compromises
===============================================================================================

[This repository](https://github.com/smarr/selfopt-interp-performance/tree/papers/zero-overhead-mop)
contains the performance evaluation setup and measurement results for the 
[Zero-Overhead Metaprogramming](http://stefan-marr.de/papers/pldi-marr-et-al-zero-overhead-metaprogramming)
paper published at PLDI. The repository and its scripts are meant to facilitate
simple re-execution of the experiments in order to reproduce and verify the
performance numbers given in the paper.

1. Downloads
------------

In addition to this repository, we provide the original data sets, a VirtualBox
image with the setup, and a complete source tarball.

 - [original data set](http://stefan-marr.de/papers/pldi-marr-et-al-zero-overhead-metaprogramming-artifacts/benchmark-and-compilation-data.tar.bz2),
   the raw data of our benchmark measurements, and compilation logs with the
   generated native code
 - [VirtualBox image with experiment setup](http://stefan-marr.de/papers/pldi-marr-et-al-zero-overhead-metaprogramming-artifacts/zero-overhead-metaprogramming-virtualbox-image.tar.bz2),
   a virtual machine with all software dependencies to facilitate reexecution of
   experiments
 - [complete source tarball](http://stefan-marr.de/papers/pldi-marr-et-al-zero-overhead-metaprogramming-artifacts/source-snapshot.tar.bz2),
   a copy of all source of this repository and its submodules

2. Setup of Experiments
-----------------------

To reexecute and verify our experiments, we provide the VirtualBox image above
as well as a set of instructions to setup the experiments on another system.
Note, the additional virtualization level of VirtualBox can have an impact on
the benchmark results.

### 2.1 VirtualBox Image

The VirtualBox image contains all software dependencies, the repository with
the experiments, and the necessary compiled binaries. Thus, it allows a direct
reexecution of the experiments without additional steps. The image was created
with VirtualBox 4.3 and contains a minimal Ubuntu 14.10 desktop.

 - download: [VirtualBox Image for Zero-Overhead Metaprogramming paper](http://stefan-marr.de/papers/pldi-marr-et-al-zero-overhead-metaprogramming-artifacts/zero-overhead-metaprogramming-virtualbox-image.tar.bz2)
 - username: zero
 - password: zero

### 2.2 Setup Instructions for other Systems

The general software requirements are as follows:

 - Python 2.7, for build tools and benchmark execution
 - C++ compiler (GCC or Clang)
 - ReBench (>= 0.7.1), for benchmark execution
 - Java 7 and 8, for Graal and Truffle
 - git, to checkout the source repositories
 - libffi headers, for RPython
 - make and maven, for the compiling the experiments
 - PyPy, to compile the RPython-based experiments
 - pip and SciPy, for the ReBench benchmarking tool


#### 2.2.1 Ubuntu

On a Ubuntu system, the following packages are required:

```bash
sudo apt-get install g++ git libffi-dev make maven \
     openjdk-7-jdk openjdk-7-source \
     openjdk-8-jdk openjdk-8-source \
     pypy python-pip python-scipy

sudo pip install ReBench
```

On older Ubuntus, the OpenJDK 8 package might not be available. Instead,
Oracle's [Java SE Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 
can be used.

#### 2.2.2 Mac OS X

Required software:

1. The basic build tools are part of Xcode and are typically installed
   automatically, as soon as `gcc` or `make` is executed on the command line.
   
2. Java 7 and 8 for Truffle+Graal
  - [Java SE Development Kit 7 Downloads](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
  - [Java SE Development Kit 8 Downloads](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

The remaining software can be installed for instance with the Homebrew or 
MacPorts package manager.

Please note, to install SciPy the use of MacPorts is recommended.
However, SciPy is optional and ReBench should work without it, but we haven't
tested it.

**Homebrew (untested)**:

```bash
brew install ant libffi maven pypy brew-pip
brew pip ReBench
```
To install SciPy without MacPorts, please see the installation instructions on
[SciPy.org](http://www.scipy.org/install.html).

**MacPorts (untested)**:

```bash
sudo port install apache-ant libffi maven3 pypy py-pip py-scipy
sudo pip install ReBench
```

#### 2.2.3 Download and Compile Experiments

All experiments are part of this git repository, which uses submodules to
manage the dependencies between source artifacts. When cloning the repository,
ensure that the right branch is used and that the submodules are initialized
properly:

```bash
git clone --recursive -b papers/zero-overhead-mop \
          https://github.com/smarr/selfopt-interp-performance
```

After all repositories have been downloaded, the experiments can be compiled
with the `implementations/setup.sh` script. Please note that this will require
further downloads. For instance the RPython-based experiments will download the
RPython sources automatically, and the JRuby experiments will download all
necessary dependencies with Maven. The whole compilation process will take a
good while. In case of errors, each part can be started separately with the
corresponding `build-$part.sh` script used in `setup.sh`.

To start the compilation:

```bash
cd implementations
./setup.sh
```
 
3. Reexecution Instructions
---------------------------

To reexecute the benchmarks on a different system and independently verify our
measurements, either the VirtualBox image with the complete setup is necessary
or a successful built of the experiments in this repository. The built
instructions are detailed in the previous section.

To execute the benchmarks, we use the [ReBench](https://github.com/smarr/ReBench)
benchmarking tool. The experiments and all benchmark parameters are configured
in the `zero-overhead.conf` file. The file has three main sections,
`benchmark_suites`, `virtual_machines`, and `experiments`. They describe the
settings for all experiments. Each of them is annotated with the section or
figure of the paper in which the results are discussed. Note that the names
used in the configuration file are post-processed for the paper in the R
scripts used to generate graphs, thus, the configuration contains all necessary
information to find the benchmark implementations in the repositories, but does
not match exactly the names in the paper.

To reexecute the experiments, ReBench is used as follows. Two important
parameter to ReBench are the `-d` switch, which shows debug output, and the
`-N` switch which disables the use of the `nice` command to increase the
process priority of the benchmarks. The `-N` is only necessary when root or sudo
are not available.

To run the benchmarks:

```bash
cd selfopt-interp-performance # change into the folder of this repository

# to run the benchmarks discussed in section 2.2:
sudo rebench -d zero-overhead.conf JavaReflection
sudo rebench -d zero-overhead.conf PyPyReflection

# to run the benchmarks shown in figure 4:
sudo rebench -d zero-overhead.conf OMOP-Micro

# to run the benchmarks shown in figure 5:
sudo rebench -d zero-overhead.conf OMOP-Standard

# to run the benchmarks shown in figure 6:
sudo rebench -d zero-overhead.conf JRuby

# to run the benchmarks shown in figure 7:
sudo rebench -d zero-overhead.conf Reflection
```

All benchmarks results are recorded in the `zero-overhead.data` file. The
benchmarks can be interrupted at any point and ReBench will continue the
execution where it left off. However, the results of partial runs of one
virtual machine invocation are not recorded to avoid mixing up results from
before and after the warmup phases.

4. Evaluation of Performance Results
------------------------------------

After the execution of the benchmarks, we evaluate the results using R. 
Our measurement raw data are part of this repository and available in
`data/zero-overhead.data.bz2`. Next to the data file is the `data/spec.md` file,
which contains the basic information on the benchmark machine we used.

An annotated version of the R script used for the evaluation is given in
`evaluation.Rmd`. It gives a brief step-by-step description how the raw data
is processed and how the graphs are generated.

The result can be rendered by executing `./scripts/knit.R evaluation.Rmd`.
However, this requires R and Knitr, as well as a variety of R packages. We
leave out the setup instructions here for brevity.

5. Generated Code of Microbenchmarks
------------------------------------

In section 4.3 of the paper, we observe in figure 4 two outliers on the
microbenchmarks. To verify that the compilers of SOM_MT and SOM_PE are able to
optimize to the same degree, independently of the presence or absence of the
OMOP metaobject protocol, we inspected the compilation results and compared the
generated code.

The compilation logs for the microbenchmarks can be created by executing the
`scripts/collect-compilation-logs.sh` script. Our log files are available in
the `data/compilation-logs.tar.bz2` file in this repository, as well as the
[data set download](http://stefan-marr.de/papers/pldi-marr-et-al-zero-overhead-metaprogramming-artifacts/benchmark-and-compilation-data.tar.bz2).

Here, we briefly pick out the two outliers and explain how to read the
compilation logs.

### 5.1 Outlier 1: Slow Field Write on SOM_MT

The field write benchmark is implemented in the `AddFieldWrite.som` file. The
corresponding log file is `AddFieldWrite.log` for the version without the
metaobject protocol. The generated code for the benchmark that is executed with
the metaobject protocol is recorded in the `AddFieldWriteEnforced.log` file.

The relevant part of the benchmark is the following code:

```
Mirror evaluate: [
    1 to: 20000 do: [ :i | 
        obj incOnce: 1
    ].
] enforcedIn: domain.
obj get = 40000 ifFalse: [
    self error: 'Benchmark failed with wrong result']
```

These log files contain the traces as well as the native code. Here we focus on
the traces since that is the level on which the optimizer works. As a first
step we determine which trace contains the main loop, and is executed during
the peak-performance measurement. For microbenchmarks, the driver loop of the
benchmark harness is typically the last one to be compiled, and thus at the end
of the file. In this case it is `Loop 4`, which starts at the following line in
the log file:

```python
# Loop 4 (Benchmark>>$blockMethod@169@12 while <WhileMessageNode object at 0x7f7f8aca8160>: Benchmark>>$blockMethod@170@17) : loop with 119 ops
```

Since the microbenchmark itself contains another loop, we need to look in this
trace for a call to other compiled code. Because of loop unrolling, there is
usually more than one call. In this case, the relevant call is:

```python
call_assembler(20000, 1, ConstPtr(ptr4), p63, ConstPtr(ptr71), descr=<Loop1>)
```

This tells us that the main benchmark loop is `Loop 1`, which starts with the
following line:

```python
# Loop 1 (#to:do: AddFieldWrite>>$blockMethod@8@16:) : loop with 48 ops
```

In this case, the loop was unrolled once, and the performance relevant part is
listed below with explanatory comments:

```python
# head of the loop, and target for back jump
+383: label(i0, i23, p3, p4, p26, p11, p9, descr=TargetToken(140185749742160))

# debug_merge_points only facilitate understanding of traces
# here we see from which SOM methods the residual code originates
debug_merge_point(0, 0, '#to:do: AddFieldWrite>>$blockMethod@8@16:')
debug_merge_point(1, 1, 'AddFieldWrite>>#$blockMethod@8@16:')

# a guard to check that the code is still valid
+403: guard_not_invalidated(descr=<Guard0x7f7f89d31bb0>) [i23, i0, p4, p3]
debug_merge_point(2, 2, 'AddFieldWriteObj>>#incTwice:')

# reading the integer value inside the #incOnce method
+403: i27 = getfield_gc_pure(p26, descr=<FieldS som.vmobjects.integer.Integer.inst__embedded_integer 8>)

# doing the `+ 1`, with overflow check
+407: i29 = int_add_ovf(i27, 1)
guard_no_overflow(descr=<Guard0x7f7f89d31b40>) [i23, i0, p4, p3, p11, i27, i29]

# doing the second `+ 1`, with overflow check
+420: i31 = int_add_ovf(i29, 1)
guard_no_overflow(descr=<Guard0x7f7f89d31ad0>) [i23, i0, p4, p3, p11, i29, i31]

# incrementing the loop counter
+433: i32 = int_add(i23, 1)
+444: i33 = int_le(i32, i0)

# the guard that would fail once the loop counter reaches the limit
guard_true(i33, descr=<Guard0x7f7f89d31a60>) [i32, i0, p4, p3, p11, i31]
debug_merge_point(0, 0, '#to:do: AddFieldWrite>>$blockMethod@8@16:')

# creates an integer object with the new value
p34 = new_with_vtable(9666600)
+528: setfield_gc(p34, i31, descr=<FieldS som.vmobjects.integer.Integer.inst__embedded_integer 8>)

#  and stores it into the 'obj' object
+552: setfield_gc(p11, p34, descr=<FieldP som.vmobjects.object.Object.inst__field1 24>)
+556: i35 = arraylen_gc(p9, descr=<ArrayP 8>)
+556: jump(i0, i32, p3, p4, p34, p11, p9, descr=TargetToken(140185749742160))
```

For the benchmark executing with the metaobject protocol enabled, it works the
same. The relevant part of the benchmark loop is the following trace:

```python
+457: label(i0, i30, p3, p4, p33, p16, p14, descr=TargetToken(139944626571008))

# note here, this is a trace from the AddFieldWriteEnforced class
debug_merge_point(0, 0, '#to:do: AddFieldWriteEnforced>>$blockMethod@9@20:')
debug_merge_point(1, 1, 'AddFieldWriteEnforced>>#$blockMethod@9@20:')

# one guard, as in the version without the metaobject protocol
+477: guard_not_invalidated(descr=<Guard0x7f4765b66950>) [i30, i0, p4, p3]

# here we see that the metaobject protocol is executed
# first, a method execution request is processed, but does not
# leave any residual code in the trace
debug_merge_point(2, 2, 'Domain>>#requestExecutionOf:with:on:lookup:')

# now we enter the method, as in the normal execution
debug_merge_point(3, 3, 'AddFieldWriteObj>>#incOnce:')

# and now a field read request is processed.
debug_merge_point(4, 4, 'Domain>>#readField:of:')

# the first residual instruction, as in the normal execution:
# reading the field
+477: i34 = getfield_gc_pure(p33, descr=<FieldS som.vmobjects.integer.Integer.inst__embedded_integer 8>)

# doing the first `+ 1`, with overflow check
+481: i36 = int_add_ovf(i34, 1)
guard_no_overflow(descr=<Guard0x7f4765b668e0>) [i30, i0, p4, p3, p16, i34, i36]

# now, we see the metaobject protocol again, but without
# residual instructions
debug_merge_point(4, 5, 'AddFieldWriteDomain>>#write:toField:of:')

# doing the second `+ 1` on the meta level, the only difference is
# that the code generator apparently swapped the arguments
+494: i38 = int_add_ovf(1, i36)
guard_no_overflow(descr=<Guard0x7f4765b66800>) [i30, i0, p4, p3, p16, i36, i38]

# incrementing the loop counter
+508: i39 = int_add(i30, 1)
+512: i40 = int_le(i39, i0)

# the guard that would fail once the loop counter reaches the limit
guard_true(i40, descr=<Guard0x7f4765b66790>) [i39, i0, p4, p3, p16, i38]
debug_merge_point(0, 0, '#to:do: AddFieldWriteEnforced>>$blockMethod@9@20:')

# creates an integer object with the new value
p41 = new_with_vtable(9666600)
+596: setfield_gc(p41, i38, descr=<FieldS som.vmobjects.integer.Integer.inst__embedded_integer 8>)

#  and stores it into the 'obj' object
+627: setfield_gc(p16, p41, descr=<FieldP som.vmobjects.object.Object.inst__field1 24>)
+631: i42 = arraylen_gc(p14, descr=<ArrayP 8>)
+631: jump(i0, i39, p3, p4, p41, p16, p14, descr=TargetToken(139944626571008))
```

So, for the first outlier, the only difference we see in the trace is that the
second add instruction has swapped arguments. Otherwise, the code is identical
and the optimizer was able to remove all reflective overhead. The only remains
of the metaobject protocol are the debug information that enable use to read
the trace.

We attribute the performance difference observed for this benchmark to elements
outside the control of our experiment. The main goal was reached, i.e., we
enabled the optimizer to compile the code using the metaobject protocol to
essentially the same code as for the version without the metaobject protocol.

### 5.2 Outlier 2: Fast Field Read on SOM_PE

The second outlier is a field read microbenchmark that got faster when executed
with the metaobject protocol enabled.

The implementation of the benchmark is in `FieldRead.som` and
`FieldReadEnforced.som`. The logs are in the corresponding files in the
`hotspot` folder.

The relevant part of the benchmark is the following code:

```
Mirror evaluate: [
    1 to: 20000 do: [ :i | sum := sum + obj get ].
] enforcedIn: domain.

sum = 40000 ifFalse: [
     self error: 'Benchmark failed with wrong result']
```

These log files have unfortunately much fewer useful annotations. The only
comments relating to the benchmark indicate the AST nodes which correspond to
the compiled code, but that does not make it easier to find the code we are
interested in. Instead, we use the constant values used in the benchmarks as
recognizable values. The two important constants are the loop boundary 20000
(in hexadecimal: 0x4e20) and the check for the result value of 40000 (in
hexadecimal: 0x9c40). With these constants, we are able to identify four
different copies of the loop in the log file, for both versions of the
benchmark. A comparison shows they have the same instruction sequences
independent of whether the metaobject protocol is used or not. Here, we briefly
examine the main loop from the last copy of the code in the log files, assuming
that this is the the main code snippet being executed, and inlining all methods
from the driver loop all the way through to the main benchmark loop.

The first snippet is the extract for the benchmark that executes without the
metaobject protocol, slightly annotated for readability.

```nasm
# the first instruction, test, is the loop head
# and target for the back jump
0x00007f7ec8735de0: test   dword ptr [rip+0x1a9ed220],eax        # 0x00007f7ee3123006
                                              ;   {poll}

# move the 'sum' variable value from rdx temporarily to rax
0x00007f7ec8735de6: mov    rax,rdx

# the obj's field value, which is 1, resides in rbp
# the actual field read is optimized out
# add it to the sum
0x00007f7ec8735de9: add    rax,rbp
0x00007f7ec8735dec: jo     0x00007f7ec8735ef8 # overflow check
0x00007f7ec8735df2: add    r14,0x1            # increment loop counter
0x00007f7ec8735df6: mov    rdx,rax

# compare current value of loop counter with 20000
0x00007f7ec8735df9: cmp    r14,0x4e20
0x00007f7ec8735e00: jle    0x00007f7ec8735de0 # if<= jump to loop head
0x00007f7ec8735e02: cmp    rdx,0x9c40         # result should be 40000
0x00007f7ec8735e09: jne    0x00007f7ec8735e8d
```

This snippet is the benchmark executing with the metaobject protocol:

```nasm
# the code looks identical, except for the used registers
0x00007f16ef9ce600: test   dword ptr [rip+0x1a7a5a00],eax        # 0x00007f170a174006
                                              ;   {poll}
0x00007f16ef9ce606: mov    rdx,rcx            # it is rax,rdx above

0x00007f16ef9ce609: add    rdx,rdi            # above: rax,rbp
0x00007f16ef9ce60c: jo     0x00007f16ef9ce6b1
0x00007f16ef9ce612: add    rbx,0x1            # above r14
0x00007f16ef9ce616: mov    rcx,rdx            # etc...

0x00007f16ef9ce619: cmp    rbx,0x4e20
0x00007f16ef9ce620: jle    0x00007f16ef9ce600
0x00007f16ef9ce622: cmp    rcx,0x9c40
0x00007f16ef9ce629: jne    0x00007f16ef9ce7b7 
```

From those code snippets, we conclude as in the previous example that the
compilers have sufficient information to remove all reflective overhead. The
remaining difference such as different memory addresses, registers, and so on
can have a performance impact on microbenchmarks, but do not have relevant
influence on the performance of larger programs.


Licensing
---------

The material in this repository is licensed under the terms of the MIT License.
Please note, the repository links in form of submodules to other repositories
which are licensed under different terms.



