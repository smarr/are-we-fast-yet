#ifndef _NBODY_H
#define _NBODY_H

/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted based on the SOM version.
 * Copyright (c) 2023 Rochus Keller <me@rochus-keller.ch> (for C++ migration)
 */

#include "Benchmark.h"

class NBody : public Benchmark {
public:
    bool innerBenchmarkLoop(int innerIterations);

    int benchmark() {
        throw "Should never be reached";
    }

    bool verifyResult(int result) {
        throw "Should never be reached";
    }

private:
    bool verifyResult(double result, int innerIterations);

};

#endif // _NBODY_H
