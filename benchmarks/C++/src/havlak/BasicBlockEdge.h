#ifndef BASICBLOCKEDGE
#define BASICBLOCKEDGE

#include "BasicBlock.h"

using namespace std;

namespace havlak {
    class ControlFlowGraph;

    class BasicBlockEdge : public enable_shared_from_this<BasicBlockEdge> {
        private:
            shared_ptr<BasicBlock> _from;
            shared_ptr<BasicBlock> _to;
        
        public:
            BasicBlockEdge(shared_ptr<ControlFlowGraph> cfg, int fromName, int toName);
    };
}

#endif //BASICBLOCKEDGE