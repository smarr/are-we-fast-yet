#ifndef SCHEDULER
#define SCHEDULER

#include <utility>
#include "DeviceTaskDataRecord.h"
#include "TaskControlBlock.h"
#include "HandlerTaskDataRecord.h"
#include <functional>
#include <iostream>
#include "../som/Error.cpp"
#include "IdleTaskDataRecord.h"
#include "WorkerTaskDataRecord.h"

namespace richards {
    class Scheduler: public RBObject {
        private:

            shared_ptr<TaskControlBlock> _taskList;
            shared_ptr<TaskControlBlock> _currentTask;
            int _currentTaskIdentity{};
            shared_ptr<TaskControlBlock>* _taskTable;
            int _queuePacketCount;
            int _holdCount;
            int _layout;
            static const bool TRACING = false;

        public:

        Scheduler();

        void createDevice(int identity, int priority, shared_ptr<Packet> workPacket, shared_ptr<TaskState> state) ;
        void createHandler(int identity, int priority,
            shared_ptr<Packet> workPaket, shared_ptr<TaskState> state);

        void createIdler(int identity, int priority, shared_ptr<Packet> work,
            shared_ptr<TaskState> state);
        shared_ptr<Packet> createPacket(shared_ptr<Packet> link, int identity, int kind);
        void createTask(int identity, int priority,
            shared_ptr<Packet> work, shared_ptr<TaskState> state,
            function<shared_ptr<TaskControlBlock>(shared_ptr<Packet> work, shared_ptr<RBObject> word)> aBlock,
            shared_ptr<RBObject> data);

        void createWorker(int identity, int priority,
            shared_ptr<Packet> workPaket, shared_ptr<TaskState> state);
        bool start();
        shared_ptr<TaskControlBlock> findTask(int identity);
        shared_ptr<TaskControlBlock> holdSelf();
        shared_ptr<TaskControlBlock> queuePacket(shared_ptr<Packet> packet);
        shared_ptr<TaskControlBlock> release(int identity);
        void trace(int id);
        shared_ptr<TaskControlBlock> markWaiting();
        void schedule();
    };
}

#endif //SCHEDULER