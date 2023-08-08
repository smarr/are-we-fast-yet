#include "LoopStructureGraph.h"

using namespace std;

namespace havlak {

    
    LoopStructureGraph::LoopStructureGraph() {
        _loopCounter = 0;
        _loops = make_shared<Vector<shared_ptr<SimpleLoop>>>();
        _root = make_shared<SimpleLoop>(nullptr, true);
        _root->setNestingLevel(0);
        _root->setCounter(_loopCounter);
        _loopCounter += 1;
        _loops->append(_root);
    }

    shared_ptr<SimpleLoop> LoopStructureGraph::createNewLoop(shared_ptr<BasicBlock> bb, bool isReducible) {
        shared_ptr<SimpleLoop> loop = make_shared<SimpleLoop>(bb, isReducible);
        loop->setCounter(_loopCounter);
        _loopCounter += 1;
        _loops->append(loop);
        return loop;
    }

    void LoopStructureGraph::calculateNestingLevel() {
        // link up all 1st level loops to artificial root node.
        _loops->forEach([&](shared_ptr<SimpleLoop> liter) -> void {
            if (!liter->isRoot()) {
                if (liter->getParent() == nullptr) {
                    liter->setParent(_root);
                }
            }
        });

        // recursively traverse the tree and assign levels.
        calculateNestingLevelRec(_root, 0);
    }

    void LoopStructureGraph::calculateNestingLevelRec(shared_ptr<SimpleLoop> loop, int depth) {
        loop->setDepthLevel(depth);
        for (auto liter : loop->getChildren()) {
            calculateNestingLevelRec(liter, depth + 1);

            loop->setNestingLevel(std::max(loop->getNestingLevel(),
                    1 + liter->getNestingLevel()));
        }
    }

    int LoopStructureGraph::getNumLoops() {
        return _loops->size();
    }

}