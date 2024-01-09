#include "BasicBlock.h"
#include "ControlFlowGraph.h"

using namespace std;

namespace havlak {

    BasicBlockEdge::BasicBlockEdge(shared_ptr<ControlFlowGraph> cfg, int fromName, int toName) {
        _from = cfg->createNode(fromName);
        _to   = cfg->createNode(toName);

        _from->addOutEdge(_to);
        _to->addInEdge(_from);
    }

}