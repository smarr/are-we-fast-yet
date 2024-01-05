#ifndef NBODY
#define NBODY

#include "Benchmark.h"
#include "nbody/NBodySystem.h"
#include "som/Error.cpp"

using namespace std;

namespace nbody {
    class NBody : public Benchmark {
        private:
            bool verifyResult(double result, int innerIterations);

        public: 
            bool innerBenchmarkLoop(int innerIterations) override;
            any benchmark() override;
            bool verifyResult(any result) override;
    };
}

#endif //NBODY