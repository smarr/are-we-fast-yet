#include "Benchmark.cpp"
#include "richards/Scheduler.cpp"

namespace richards {
    class Richards : public Benchmark {
        
        public: 

            bool verifyResult(std::any result) override {
                bool result_cast = std::any_cast<bool>(result);
                return result_cast;
            }

            std::any benchmark() override {
                return (Scheduler()).start();
            }
    };
};