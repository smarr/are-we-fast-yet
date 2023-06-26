/* This code is derived from the SOM benchmarks, see AUTHORS.md file.
 *
 * Copyright (c) 2023 Rochus Keller <me@rochus-keller.ch> (for C++ migration)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the 'Software'), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

#include "Run.h"
#include <time.h>
#include <stdio.h>
#include "Bounce.h"
#include "List.h"
#include "Mandelbrot.h"
#include "Permute.h"
#include "Queens.h"
#include "Sieve.h"
#include "Storage.h"
#include "Towers.h"
#include "NBody.h"
#include "Richards.h"
#include "Json.h"
#include "CD.h"
#include "Havlak.h"
#include "DeltaBlue.h"

#if defined(_WIN32) && !defined(__GNUC__)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>

// Source: https://stackoverflow.com/questions/10905892/equivalent-of-gettimeday-for-windows/26085827

// MSVC defines this in winsock2.h!?
typedef struct timeval {
    long tv_sec;
    long tv_usec;
} timeval;

int gettimeofday(struct timeval * tp, struct timezone * tzp)
{
    // Note: some broken versions only have 8 trailing zero's, the correct epoch has 9 trailing zero's
    // This magic number is the number of 100 nanosecond intervals since January 1, 1601 (UTC)
    // until 00:00:00 January 1, 1970
    static const uint64_t EPOCH = ((uint64_t) 116444736000000000ULL);

    SYSTEMTIME  system_time;
    FILETIME    file_time;
    uint64_t    time;

    GetSystemTime( &system_time );
    SystemTimeToFileTime( &system_time, &file_time );
    time =  ((uint64_t)file_time.dwLowDateTime )      ;
    time += ((uint64_t)file_time.dwHighDateTime) << 32;

    tp->tv_sec  = (long) ((time - EPOCH) / 10000000L);
    tp->tv_usec = (long) (system_time.wMilliseconds * 1000);
    return 0;
}
#else
#include <sys/time.h>
#endif


Benchmark *Run::getSuiteFromName(const std::string &name)
{
    if( name == "Bounce" )
        return new Bounce();
    if( name == "List" )
        return new List();
    if( name == "Mandelbrot" )
        return new Mandelbrot();
    if( name == "Permute" )
        return new Permute();
    if( name == "Queens" )
        return new Queens();
    if( name == "Sieve" )
        return new Sieve();
    if( name == "Storage" )
        return new Storage();
    if( name == "Towers" )
        return new Towers();
    if( name == "NBody" )
        return new NBody();
    if( name == "Richards" )
        return new Richards();
    if( name == "Json" )
        return new Json();
    if( name == "CD" )
        return new CD();
    if( name == "Havlak" )
        return new Havlak();
    if( name == "DeltaBlue" )
        return new DeltaBlue();
    return 0;
}

Run::Run(const std::string &name):numIterations(0),innerIterations(0),total(0)
{
    this->name = name;
    this->benchmarkSuite = getSuiteFromName(name);
    numIterations   = 1;
    innerIterations = 1;
}

Run::~Run()
{
    if( benchmarkSuite )
        delete benchmarkSuite;
}

void Run::runBenchmark()
{
    if( benchmarkSuite == 0 )
    {
        std::cerr << "ERROR unknown benchmark "<< name << std::endl;
        throw "";
    }

    // Checkstyle: stop
    std::cout << "Starting " << name << " benchmark ..." << std::endl;
    // Checkstyle: resume

    doRuns(benchmarkSuite);
    reportBenchmark();

    // Checkstyle: stop
    std::cout << std::endl;
    // Checkstyle: resume
}

void Run::measure(Benchmark *bench)
{
    struct timeval start, end;
    gettimeofday(&start, 0);
    if (!bench->innerBenchmarkLoop(innerIterations)) {
        std::cerr << "Benchmark failed with incorrect result" << std::endl;
        return;
    }
    gettimeofday(&end, 0);
    const long seconds = end.tv_sec - start.tv_sec;
    const long microseconds = end.tv_usec - start.tv_usec;
    const long runTime = seconds*1000000 + microseconds; // us

    printResult(runTime);

    total += runTime;
}

void Run::doRuns(Benchmark *bench)
{
    for (int i = 0; i < numIterations; i++) {
        measure(bench);
    }
}

void Run::reportBenchmark()
{
    // Checkstyle: stop
    std::cout << name << ": iterations=" << numIterations <<
                 " average: " << (total / numIterations) << "us total: " << total << "us" << std::endl;
    // Checkstyle: resume

}

void Run::printResult(long runTime)
{
#if 0
    // Checkstyle: stop
    std::cout << name << ": iterations=1 runtime: " << runTime << "us" << std::endl;
    // Checkstyle: resume
#endif

}

void Run::printTotal()
{
#if 0
    // Checkstyle: stop
    std::cout << "Total Runtime: " << total << "us" << std::endl;
    // Checkstyle: resume
#endif
}
