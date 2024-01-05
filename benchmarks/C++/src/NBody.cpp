#include "NBody.h"

using namespace std;

namespace nbody {

    bool NBody::verifyResult(double result, int innerIterations) {
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


    bool NBody::innerBenchmarkLoop(int innerIterations) {
        shared_ptr<NBodySystem> system = make_shared<NBodySystem>();
        for (int i = 0; i < innerIterations; i++) {
            system->advance(0.01);
        }

        return verifyResult(system->energy(), innerIterations);
    }

    any NBody::benchmark() {
        throw Error("Should never be reached");
    }

    bool NBody::verifyResult(any result) {
        (void)result;
        throw Error("Should never be reached");
    }
}
