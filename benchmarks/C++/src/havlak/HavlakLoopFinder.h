#ifndef HAVLAKLOOPFINDER
#define HAVLAKLOOPFINDER

#include "ControlFlowGraph.h"
#include "LoopStructureGraph.h"
#include "UnionFindNode.h"
#include <map>
#include <algorithm>
#include <iostream>
#include "../som/IdentityDictionary.cpp"

using namespace std;

namespace havlak {
    class HavlakLoopFinder {
        private:

            enum BasicBlockClass {
                BB_TOP,          // uninitialized
                BB_NONHEADER,    // a regular BB
                BB_REDUCIBLE,    // reducible loop
                BB_SELF,         // single BB loop
                BB_IRREDUCIBLE,  // irreducible loop
                BB_DEAD,         // a dead BB
                BB_LAST          // Sentinel
            };

            shared_ptr<ControlFlowGraph> _cfg;      // Control Flow Graph
            shared_ptr<LoopStructureGraph> _lsg;    // Loop Structure Graph

            static constexpr int UNVISITED = numeric_limits<int>::max();
            static constexpr int MAXNONBACKPREDS = 32 * 1024;

            shared_ptr<Vector<set<int>>> _nonBackPreds = make_shared<Vector<set<int>>>() ;
            shared_ptr<Vector<shared_ptr<Vector<int>>>> _backPreds = make_shared<Vector<shared_ptr<Vector<int>>>>();
            shared_ptr<IdentityDictionary<int>> _number = make_shared<IdentityDictionary<int>>();
            
            int _maxSize = 0;
            int* _header;
            BasicBlockClass* _type;
            int* _last;
            shared_ptr<UnionFindNode>* _nodes;
            
        public:
            HavlakLoopFinder(shared_ptr<ControlFlowGraph> cfg, shared_ptr<LoopStructureGraph> lsg);
            void findLoops();
        
        private:
            bool isAncestor(int w, int v);
            int doDFS(shared_ptr<BasicBlock> currentNode, int current);
            void initAllNodes();
            void identifyEdges(int size);
            void processEdges(shared_ptr<BasicBlock> nodeW, int w);
            void stepEProcessNonBackPreds(int w, shared_ptr<Vector<shared_ptr<UnionFindNode>>> nodePool, shared_ptr<Vector<shared_ptr<UnionFindNode>>> workList, shared_ptr<UnionFindNode> x);


            void setLoopAttributes(int w, shared_ptr<Vector<shared_ptr<UnionFindNode>>> nodePool, shared_ptr<SimpleLoop> loop);
            void stepD(int w, shared_ptr<Vector<shared_ptr<UnionFindNode>>> nodePool);



    };
}

#endif //HAVLAKLOOPFINDER