#include "SimpleLoop.h"

namespace havlak {
    
    SimpleLoop::SimpleLoop(shared_ptr<BasicBlock> bb, bool isReducible) {
        _isReducible = isReducible;
        _parent = nullptr;
        _isRoot = false;
        _nestingLevel = 0;
        _depthLevel   = 0;
        if (bb != nullptr) {
            _basicBlocks.insert(bb);
        }
        _header = bb;
    }

    void SimpleLoop::addNode(shared_ptr<BasicBlock> bb) {
        _basicBlocks.insert(bb);
    }

    void SimpleLoop::addChildLoop(shared_ptr<SimpleLoop> loop) {
        _children.insert(loop);
    }

    // Getters/Setters
    set<shared_ptr<SimpleLoop>> SimpleLoop::getChildren() {
        return _children;
    }

    shared_ptr<SimpleLoop> SimpleLoop::getParent() {
        return _parent;
    }

    int SimpleLoop::getNestingLevel() const {
        return _nestingLevel;
    }

    bool SimpleLoop::isRoot() const {
        return _isRoot;
    }

    void SimpleLoop::setParent(shared_ptr<SimpleLoop> parent) {
        _parent = parent;
        if (parent != nullptr) {
            parent->addChildLoop(shared_from_this());
        }
    }

    void SimpleLoop::setIsRoot() {
        _isRoot = true;
    }

    void SimpleLoop::setCounter(int value) {
        _counter = value;
    }

    void SimpleLoop::setNestingLevel(int level) {
        _nestingLevel = level;
        if (level == 0) {
            setIsRoot();
        }
    }

    void SimpleLoop::setDepthLevel(int level) {
        _depthLevel = level;
    }
}