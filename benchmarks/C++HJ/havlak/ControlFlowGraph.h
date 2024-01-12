#ifndef CONTROLFLOWGRAPH
#define CONTROLFLOWGRAPH

#include "BasicBlock.h"
#include "BasicBlockEdge.h"
#include "../som/Vector.cpp"
using namespace std;

namespace havlak {
    
    class ControlFlowGraph {
        private:
            shared_ptr<Vector<shared_ptr<BasicBlock>>> _basicBlockMap;
            shared_ptr<BasicBlock> _startNode;
            shared_ptr<Vector<shared_ptr<BasicBlockEdge>>> _edgeList;
        
        public:
            ControlFlowGraph();
            shared_ptr<BasicBlock> createNode(int name);
            void addEdge(shared_ptr<BasicBlockEdge> edge);
            int getNumNodes();
            shared_ptr<BasicBlock> getStartBasicBlock();
            shared_ptr<Vector<shared_ptr<BasicBlock>>> getBasicBlocks();

    };
}

#endif //CONTROLFLOWGRAPH
