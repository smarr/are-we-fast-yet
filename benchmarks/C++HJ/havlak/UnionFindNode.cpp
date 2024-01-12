#include "UnionFindNode.h"

namespace havlak {

    UnionFindNode::UnionFindNode() { } 

    // Initialize this node.
    void UnionFindNode::initNode(shared_ptr<BasicBlock> bb, int dfsNumber) {
        _parent = shared_from_this();
        _bb = bb;
        _dfsNumber = dfsNumber;
        _loop = nullptr;
    }

    // Union/Find Algorithm - The find routine.
    shared_ptr<UnionFindNode> UnionFindNode::findSet() {
        Vector<shared_ptr<UnionFindNode>> nodeList = Vector<shared_ptr<UnionFindNode>>();

        shared_ptr<UnionFindNode> node = shared_from_this();
        while (node != node->_parent) {
            if (node->_parent != node->_parent->_parent) {
                nodeList.append(node);
            }
            node = node->_parent;
        }

        // Path Compression, all nodes' parents point to the 1st level parent.
        nodeList.forEach([&](shared_ptr<UnionFindNode> iter) -> void {
            iter->unionSet(_parent);
        });
        return node;
    }

    // Union/Find Algorithm - The union routine.
    // Trivial. Assigning parent pointer is enough, we rely on path compression.
    void UnionFindNode::unionSet(shared_ptr<UnionFindNode> basicBlock) {
        _parent = basicBlock;
    }

    // Getters/Setters
    shared_ptr<BasicBlock> UnionFindNode::getBb() {
        return _bb;
    }

    shared_ptr<SimpleLoop> UnionFindNode::getLoop() {
        return _loop;
    }

    int UnionFindNode::getDfsNumber() {
        return _dfsNumber;
    }

    void UnionFindNode::setLoop(shared_ptr<SimpleLoop> loop) {
        _loop = loop;
    }
}
