#include "ControlFlowGraph.h"

#include <iostream>

namespace havlak {

    ControlFlowGraph::ControlFlowGraph() {
        _startNode = nullptr;
        _basicBlockMap = make_shared<Vector<shared_ptr<BasicBlock>>>();
        _edgeList = make_shared<Vector<shared_ptr<BasicBlockEdge>>>();
    }

    shared_ptr<BasicBlock> ControlFlowGraph::createNode(int name) {
        shared_ptr<BasicBlock> node;
        if (_basicBlockMap != nullptr && _basicBlockMap->atPtr(name) != nullptr) {
            node = _basicBlockMap->atPtr(name);
        } else {
            node = make_shared<BasicBlock>(name);
            _basicBlockMap->atPut(name, node);
        }

        if (getNumNodes() == 1) {
            _startNode = node;
        }
        return node;
    }

    void ControlFlowGraph::addEdge(shared_ptr<BasicBlockEdge> edge) {
        _edgeList->append(edge);
    }

    int ControlFlowGraph::getNumNodes() {
        return _basicBlockMap->size();
    }

    shared_ptr<BasicBlock> ControlFlowGraph::getStartBasicBlock() {
        return _startNode;
    }

    shared_ptr<Vector<shared_ptr<BasicBlock>>> ControlFlowGraph::getBasicBlocks() {
        return _basicBlockMap;
    }

}