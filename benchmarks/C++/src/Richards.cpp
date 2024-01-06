#include "Richards.h"

namespace richards {

    bool Richards::verifyResult(std::any result) {
        bool result_cast = std::any_cast<bool>(result);
        return result_cast;
    }

    std::any Richards::benchmark() {
        return (Scheduler()).start();
    }
}