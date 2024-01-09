#ifndef LOOPSTRUCTUREGRAPH
#define LOOPSTRUCTUREGRAPH

#include <vector>
#include "SimpleLoop.h"
#include "../som/Vector.cpp"

using namespace std;

namespace havlak {
    class LoopStructureGraph {
        private:
            shared_ptr<SimpleLoop> _root;
            shared_ptr<Vector<shared_ptr<SimpleLoop>>> _loops;
            int _loopCounter;
        
        public:
            
            LoopStructureGraph();

            shared_ptr<SimpleLoop> createNewLoop(shared_ptr<BasicBlock> bb, bool isReducible);
            void calculateNestingLevel();
            void calculateNestingLevelRec(shared_ptr<SimpleLoop> loop, int depth);
            int getNumLoops();
    };
}

#endif //LOOPSTRUCTUREGRAPH