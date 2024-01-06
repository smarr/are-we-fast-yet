#ifndef RICHARDS
#define RICHARDS


#include "Benchmark.h"
#include "richards/Scheduler.h"

namespace richards {
    class Richards : public Benchmark {
        
        public: 

            bool verifyResult(std::any result) override;
            std::any benchmark() override;
    };
};

#endif // RICHARDS