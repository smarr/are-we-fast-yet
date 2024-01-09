#ifndef HAVLAK
#define HAVLAK

#include "havlak/LoopTesterApp.h"
#include "Benchmark.h"
#include "som/Error.cpp"

using namespace std;

namespace havlak {
    class Havlak : public Benchmark {
        
        public:
            bool innerBenchmarkLoop(int innerIterations) override;
            bool verifyResult(any result, int innerIterations);
            any benchmark() override;
            bool verifyResult(any result) override;
    };
}

#endif //HAVLAK