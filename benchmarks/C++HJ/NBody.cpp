#include "Benchmark.cpp"
#include "nbody/NBodySystem.cpp"
#include <iostream>
#include "som/Error.cpp"
using namespace std;

namespace nbody {
    class NBody : public Benchmark {
        private:
            bool verifyResult(double result, int innerIterations) {
                if (innerIterations == 250000) {
                    return result == -0.1690859889909308;
                }
                if (innerIterations == 1) {
                    return result == -0.16907495402506745;
                }

                cout << "No verification result for " << innerIterations << " found" << endl;
                cout << "Result is: " << result << endl;
                return false;
            }


        public: 
            bool innerBenchmarkLoop(int innerIterations) override {
                shared_ptr<NBodySystem> system = make_shared<NBodySystem>();
                for (int i = 0; i < innerIterations; i++) {
                    system->advance(0.01);
                }

                return verifyResult(system->energy(), innerIterations);
            }

            any benchmark() override {
                throw Error("Should never be reached");
            }

            bool verifyResult(any result) override {
                throw Error("Should never be reached");
            }
    };
}
