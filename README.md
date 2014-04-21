Self-Optimizing Interpreters
============================

This repository contains all the necessary bits and pieces for an evaluation
of different ideas around self-optimizing interpreters.

Currently, the evaluation focusses on the following aspects:

 - _partial evaluation vs. meta-tracing_  
   What are the tradeoffs with respect to engineering effort and performance?

 - _optimization techniques and their benefits_  
   There a number of common patterns used for self-optimizing interpreters, 
   and we are interested in which concrete impact they have on performance.

 - _pure interpretation_  
   While peek-performance is one relevant criterion, startup time, 
   and pure interpretation speed are relevant in a number of scenarios as well.
   Thus, we are interested in the impact of the various optimizations on 
   interpretation speed.

 - _the future_  
   Which new and powerful language features could self-optimizing interpreters
   facilitate? We think, they provide us with a technique that makes highly dynamic
   language features such as _metaobject protocols_ practical. Here, we experiment
   with such techniques and investigate the performance potential.

Self-Contained, Complete, and Reproducible Evaluation Setup
-----------------------------------------------------------

This repository contains everything that is necessary to re-execute all
experiments. Well, that excludes obviously the hardware, and operating system
setup.

The following quick start instructions, allow to recreate the basic environment:

```bash
git clone --recursive http://github/TODO
```

The setup has been used and tested on Ubuntu and OS X.
The following programs are definitely required for execution:

 - ReBench (>= 0.5)
