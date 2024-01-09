#include "ControlFlowGraph.h"
#include "LoopStructureGraph.h"
#include "HavlakLoopFinder.h"
#include <memory>
#include <iostream>

using namespace std;

namespace havlak {
    class LoopTesterApp {

        private:

            shared_ptr<ControlFlowGraph> _cfg;
            shared_ptr<LoopStructureGraph> _lsg;

            int buildDiamond(int start);
            void buildConnect(int start, int end);
            int buildStraight(int start, int n);
            int buildBaseLoop(int from);
            void constructSimpleCFG();

        public:
            LoopTesterApp();
            vector<int> main(int numDummyLoops, int findLoopIterations, int parLoops,
                    int pparLoops, int ppparLoops);
            void constructCFG(int parLoops, int pparLoops, int ppparLoops);
            void addDummyLoops(int numDummyLoops);
            void findLoops(shared_ptr<LoopStructureGraph> loopStructure);
    };
}