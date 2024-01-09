#include "Havlak.h"

namespace havlak {
    bool Havlak::innerBenchmarkLoop(int innerIterations) {
        bool result = verifyResult((make_shared<LoopTesterApp>())->main(
            innerIterations, 50, 10 /* was 100 */, 10, 5), innerIterations);
        return result;
    } 

    bool Havlak::verifyResult(any result, int innerIterations) {
        vector<int> r = any_cast<vector<int>>(result);
        if (innerIterations == 15000) { return r[0] == 46602 && r[1] == 5213; }
        if (innerIterations ==  1500) { return r[0] ==  6102 && r[1] == 5213; }
        if (innerIterations ==   150) { return r[0] ==  2052 && r[1] == 5213; }
        if (innerIterations ==    15) { return r[0] ==  1647 && r[1] == 5213; }
        if (innerIterations ==     1) { return r[0] ==  1605 && r[1] == 5213; }


        cout << "No verification result for " << innerIterations << " found" << endl;
        cout << "Result is: " << r[0] << ", " <<  r[1] << endl;

        return false;
    }

    any Havlak::benchmark() {
        throw Error("Should never be reached");
    }

    
    bool Havlak::verifyResult(any result) {
        (void)result;
        throw Error("Should never be reached");
    }
}