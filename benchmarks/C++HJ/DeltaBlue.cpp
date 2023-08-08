#include "Benchmark.cpp"
#include "som/Error.cpp"
#include "deltablue/Planner.h"
#include <iostream>

using namespace std;

namespace deltablue {
    class DeltaBlue : public Benchmark {
        public:
            bool innerBenchmarkLoop(int innerIterations) override {
                Planner::chainTest(innerIterations);
                Planner::projectionTest(innerIterations);
                return true;
            } 

            any benchmark() override {
                throw Error("should never be reached");
            }
    
            bool verifyResult(any result) override {
                throw Error("should never be reached");
            }
    };
}   