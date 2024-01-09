#ifndef SIMPLELOOP
#define SIMPLELOOP

#include <set>
#include "BasicBlock.h"

using namespace std;

namespace havlak {
    class SimpleLoop : public enable_shared_from_this<SimpleLoop> {
        private:
            set<shared_ptr<BasicBlock>> _basicBlocks;
            set<shared_ptr<SimpleLoop>> _children;
            shared_ptr<SimpleLoop> _parent;
            shared_ptr<BasicBlock> _header;
            bool _isReducible;
            bool _isRoot;
            int _nestingLevel;
            int _counter;
            int _depthLevel;

        public:
            SimpleLoop(shared_ptr<BasicBlock> bb, bool isReducible);

            void addNode(shared_ptr<BasicBlock> bb);
            void addChildLoop(shared_ptr<SimpleLoop> loop);
            set<shared_ptr<SimpleLoop>> getChildren();
            shared_ptr<SimpleLoop> getParent();
            int getNestingLevel() const;
            bool isRoot() const;
            void setParent(shared_ptr<SimpleLoop> parent);
            void setIsRoot();
            void setCounter(int value);
            void setNestingLevel(int level);
            void setDepthLevel(int level);
    };
}

#endif //SIMPLELOOP