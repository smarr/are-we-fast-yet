#ifndef UNIONFINDNODE
#define UNIONFINDNODE

#include <vector>
#include "SimpleLoop.h"
#include <memory>
#include <iostream>

using namespace std;

namespace havlak {
    class UnionFindNode : public enable_shared_from_this<UnionFindNode> {
        private:
            shared_ptr<UnionFindNode> _parent;
            shared_ptr<BasicBlock> _bb;
            shared_ptr<SimpleLoop> _loop;
            int _dfsNumber;

        public:
            UnionFindNode();

            void initNode(shared_ptr<BasicBlock> bb, int dfsNumber);
            shared_ptr<UnionFindNode> findSet();
            void unionSet(shared_ptr<UnionFindNode> basicBlock);
            shared_ptr<BasicBlock> getBb();
            shared_ptr<SimpleLoop> getLoop();
            int getDfsNumber();
            void setLoop(shared_ptr<SimpleLoop> loop);
    };
}

#endif //UNIONFINDNODE

