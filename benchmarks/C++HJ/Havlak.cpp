#include "havlak/LoopTesterApp.cpp"
#include "Benchmark.cpp"
#include <iostream>
#include "som/Error.cpp"

using namespace std;

namespace havlak {
    class Havlak : public Benchmark {
        public:
            bool innerBenchmarkLoop(int innerIterations) override {
                bool result = verifyResult((make_shared<LoopTesterApp>())->main(
                    innerIterations, 50, 10 /* was 100 */, 10, 5), innerIterations);
                return result;
            } 

<<<<<<< Updated upstream
            bool verifyResult(any result, int innerIterations) {
                vector<int> r = any_cast<vector<int>>(result);
=======
            bool verifyResult(vector<int> r, int innerIterations) {
>>>>>>> Stashed changes
                if (innerIterations == 15000) { return r[0] == 46602 && r[1] == 5213; }
                if (innerIterations ==  1500) { return r[0] ==  6102 && r[1] == 5213; }
                if (innerIterations ==   150) { return r[0] ==  2052 && r[1] == 5213; }
                if (innerIterations ==    15) { return r[0] ==  1647 && r[1] == 5213; }
                if (innerIterations ==     1) { return r[0] ==  1605 && r[1] == 5213; }


                cout << "No verification result for " << innerIterations << " found" << endl;
                cout << "Result is: " << r[0] << ", " <<  r[1] << endl;

                return false;
            }

            any benchmark() override {
                throw Error("Should never be reached");
            }

            
            bool verifyResult(any result) override {
                throw Error("Should never be reached");
            }
    };
}