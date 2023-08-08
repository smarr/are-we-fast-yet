#ifndef _RICHARDS_H
#define _RICHARDS_H

/*
 * Copyright (c) 2023 Rochus Keller <me@rochus-keller.ch> (for C++ migration)
 *
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * Richards.
 *
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */

#include "Benchmark.h"

class Richards : public Benchmark {
public:
    int benchmark();

    bool verifyResult(int result) {
        return result != 0;
    }
};

#endif // _RICHARDS_H
