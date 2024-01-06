#ifndef TASKCONTROLBLOCK
#define TASKCONTROLBLOCK

#include "TaskState.h"
#include <memory>
#include "Packet.h"
#include <functional>
#include <utility>
#include <iostream>
#include <memory>

using namespace std;

namespace richards {
    class TaskControlBlock : public TaskState, public enable_shared_from_this<TaskControlBlock> {
        private: 

            shared_ptr<TaskControlBlock> _link;
            int _identity;
            int _priority;
            shared_ptr<Packet> _input;
            function<shared_ptr<TaskControlBlock>(shared_ptr<Packet> work, shared_ptr<RBObject> word)> _function;
            shared_ptr<RBObject> _handle;

        public:

            TaskControlBlock(shared_ptr<TaskControlBlock> aLink, int anIdentity, int aPriority, shared_ptr<Packet> anInitialWorkQueue,
                    const shared_ptr<TaskState>& anInitialState, function<shared_ptr<TaskControlBlock>(shared_ptr<Packet> work, shared_ptr<RBObject> word)> aBlock,
                    shared_ptr<RBObject> aPrivateData);

            int getIdentity() const;
            shared_ptr<TaskControlBlock> getLink();
            int getPriority() const;
            shared_ptr<TaskControlBlock> addInputAndCheckPriority(shared_ptr<Packet> packet, shared_ptr<TaskControlBlock> oldTask);
            shared_ptr<TaskControlBlock> runTask();
    };

}

#endif //TASKCONTROLBLOCK