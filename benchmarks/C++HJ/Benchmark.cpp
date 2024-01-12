#ifndef BENCHMARK
#define BENCHMARK

#include <any>
#include <memory>
#include <iostream>
using namespace std;


class Benchmark {
    public: 
        virtual any benchmark() = 0;
        virtual bool verifyResult(any result) = 0;

        virtual bool innerBenchmarkLoop(int innerIterations) {
            for (int i = 0; i < innerIterations; i++) {
                if (!verifyResult(benchmark())) {
                    return false;
                }
            }
            return true;
        }
};

#endif //BENCHMARK