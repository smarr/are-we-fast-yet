#include "Scheduler.h"

namespace richards {
    Scheduler::Scheduler() {
        RBObject::initializeConstants();
        // init tracing
        _layout  = 0;

        // init scheduler
        _queuePacketCount = 0;
        _holdCount = 0;
        _taskTable = new shared_ptr<TaskControlBlock>[NUM_TYPES];

        for (int i = 0; i < NUM_TYPES; i++) {
            _taskTable[i] = RBObject::NO_TASK;
        }
        _taskList = RBObject::NO_TASK;
    }


    void Scheduler::createDevice(int identity, int priority, shared_ptr<Packet> workPacket, shared_ptr<TaskState> state) {
        shared_ptr<DeviceTaskDataRecord> data = make_shared<DeviceTaskDataRecord>();

        createTask(identity, priority, workPacket, state,
                    [&](shared_ptr<Packet> workArg, shared_ptr<RBObject> wordArg) -> shared_ptr<TaskControlBlock> {
        shared_ptr<DeviceTaskDataRecord> dataRecord = dynamic_pointer_cast<DeviceTaskDataRecord>(wordArg);
        shared_ptr<Packet> functionWork = workArg;
        if (RBObject::NO_WORK == functionWork) {
            if (RBObject::NO_WORK == (functionWork = dataRecord->getPending())) {
                return markWaiting();
            } else {
                dataRecord->setPending(RBObject::NO_WORK);
                return queuePacket(functionWork);
            }
        } else {
            dataRecord->setPending(functionWork);
                if (TRACING) {
                    trace(functionWork->getDatum());
                }
                return holdSelf();
            }
        }, data);
    }

    void Scheduler::createHandler(int identity, int priority,
        shared_ptr<Packet> workPaket, shared_ptr<TaskState> state) {

        shared_ptr<HandlerTaskDataRecord> data = make_shared<HandlerTaskDataRecord>();

        createTask(identity, priority, workPaket, state,
                    [&](shared_ptr<Packet> work, shared_ptr<RBObject> word) -> shared_ptr<TaskControlBlock> {
            shared_ptr<HandlerTaskDataRecord> dataRecord = dynamic_pointer_cast<HandlerTaskDataRecord>(word);
            if (RBObject::NO_WORK != work) {
                if (WORK_PACKET_KIND == work->getKind()) {
                dataRecord->workInAdd(work);
                } else {
                dataRecord->deviceInAdd(work);
                }
            }

            shared_ptr<Packet> workPacket;
            if (RBObject::NO_WORK == (workPacket = dataRecord->workIn())) {
                return markWaiting();
            } else {
                int count = workPacket->getDatum();
                if (count >= Packet::DATA_SIZE) {
                    dataRecord->workIn(workPacket->getLink());
                    return queuePacket(workPacket);
                } else {
                    shared_ptr<Packet> devicePacket;
                    if (RBObject::NO_WORK == (devicePacket = dataRecord->deviceIn())) {
                        return markWaiting();
                    } else {
                        dataRecord->deviceIn(devicePacket->getLink());
                        devicePacket->setDatum(workPacket->getData()[count]);
                        workPacket->setDatum(count + 1);
                        return queuePacket(devicePacket);
                    }
                }
            }
        }, data);
    }

    void Scheduler::createIdler(int identity, int priority, shared_ptr<Packet> work,
        shared_ptr<TaskState> state) {

        shared_ptr<IdleTaskDataRecord> data = make_shared<IdleTaskDataRecord>();

        createTask(identity, priority, work, state,
                    [&](shared_ptr<Packet> workArg, shared_ptr<RBObject> wordArg) -> shared_ptr<TaskControlBlock> {
            shared_ptr<IdleTaskDataRecord> dataRecord = dynamic_pointer_cast<IdleTaskDataRecord>(wordArg);
            dataRecord->setCount(dataRecord->getCount() - 1);
            if (0 == dataRecord->getCount()) {
                return holdSelf();
            } else {
                if (0 == (dataRecord->getControl() & 1)) {
                    dataRecord->setControl(dataRecord->getControl() / 2);
                    return release(DEVICE_A);
                } else {
                    dataRecord->setControl((dataRecord->getControl() / 2) ^ 53256);
                    return release(DEVICE_B);
                }
            }
        }, data);
    }

    shared_ptr<Packet> Scheduler::createPacket(shared_ptr<Packet> link, int identity, int kind) {
        return make_shared<Packet>(link, identity, kind);
    }

    void Scheduler::createTask(int identity, int priority,
        shared_ptr<Packet> work, shared_ptr<TaskState> state,
        function<shared_ptr<TaskControlBlock>(shared_ptr<Packet> work, shared_ptr<RBObject> word)> aBlock,
        shared_ptr<RBObject> data) {

        shared_ptr<TaskControlBlock> t = make_shared<TaskControlBlock>(_taskList, identity,
            priority, work, state, aBlock, data);
        _taskList = t;
        _taskTable[identity] = t;
    }

    void Scheduler::createWorker(int identity, int priority,
        shared_ptr<Packet> workPaket, shared_ptr<TaskState> state) {

        shared_ptr<WorkerTaskDataRecord> dataRecord = make_shared<WorkerTaskDataRecord>();

        createTask(identity, priority, workPaket, state,
            [&](shared_ptr<Packet> work, shared_ptr<RBObject> word) -> shared_ptr<TaskControlBlock> {
            shared_ptr<WorkerTaskDataRecord> data = dynamic_pointer_cast<WorkerTaskDataRecord>(word);
            if (RBObject::NO_WORK == work) {
                return markWaiting();
            } else {
                data->setDestination((HANDLER_A == data->getDestination()) ? HANDLER_B : HANDLER_A);
                work->setIdentity(data->getDestination());
                work->setDatum(0);
                for (int i = 0; i < Packet::DATA_SIZE; i++) {
                    data->setCount(data->getCount() + 1);
                    if (data->getCount() > 26) { 
                        data->setCount(1); 
                    }
                    work->getData()[i] = 65 + data->getCount() - 1;
                }
                return queuePacket(work);
            }
        }, dataRecord);
    }

    bool Scheduler::start() {
        shared_ptr<Packet> workQ;
        createIdler(IDLER, 0, RBObject::NO_WORK, TaskState::createRunning());
        workQ = createPacket(RBObject::NO_WORK, WORKER, WORK_PACKET_KIND);
        workQ = createPacket(workQ,   WORKER, WORK_PACKET_KIND);

        createWorker(WORKER, 1000, workQ, TaskState::createWaitingWithPacket());
        workQ = createPacket(RBObject::NO_WORK, DEVICE_A, DEVICE_PACKET_KIND);
        workQ = createPacket(workQ,   DEVICE_A, DEVICE_PACKET_KIND);
        workQ = createPacket(workQ,   DEVICE_A, DEVICE_PACKET_KIND);

        createHandler(HANDLER_A, 2000, workQ, TaskState::createWaitingWithPacket());
        workQ = createPacket(RBObject::NO_WORK, DEVICE_B, DEVICE_PACKET_KIND);
        workQ = createPacket(workQ, DEVICE_B, DEVICE_PACKET_KIND);
        workQ = createPacket(workQ, DEVICE_B, DEVICE_PACKET_KIND);

        createHandler(HANDLER_B, 3000, workQ, TaskState::createWaitingWithPacket());
        createDevice(DEVICE_A, 4000, RBObject::NO_WORK, TaskState::createWaiting());
        createDevice(DEVICE_B, 5000, RBObject::NO_WORK, TaskState::createWaiting());

        schedule();

        return _queuePacketCount == 23246 && _holdCount == 9297;
    }

    shared_ptr<TaskControlBlock> Scheduler::findTask(int identity) {
        shared_ptr<TaskControlBlock> t = _taskTable[identity];
        if (RBObject::NO_TASK == t) { 
            throw Error("findTask failed");
        }
        return t;
    }

    shared_ptr<TaskControlBlock> Scheduler::holdSelf() {
        _holdCount = _holdCount + 1;
        _currentTask->setTaskHolding(true);
        return _currentTask->getLink();
    }

    shared_ptr<TaskControlBlock> Scheduler::queuePacket(shared_ptr<Packet> packet) {
        shared_ptr<TaskControlBlock> t = findTask(packet->getIdentity());
        if (RBObject::NO_TASK == t) { 
            return RBObject::NO_TASK; 
        }

        _queuePacketCount = _queuePacketCount + 1;

        packet->setLink(RBObject::NO_WORK);
        packet->setIdentity(_currentTaskIdentity);
        return t->addInputAndCheckPriority(packet, _currentTask);
    }

    shared_ptr<TaskControlBlock> Scheduler::release(int identity) {
        shared_ptr<TaskControlBlock> t = findTask(identity);
        if (RBObject::NO_TASK == t) { 
            return RBObject::NO_TASK; 
        }
        t->setTaskHolding(false);
        if (t->getPriority() > _currentTask->getPriority()) {
            return t;
        } else {
            return _currentTask;
        }
    }

    void Scheduler::trace(int id) {
        _layout = _layout - 1;
        if (0 >= _layout) {
            cout << endl;
            _layout = 50;
        }
        cout << id << endl;
    }

    shared_ptr<TaskControlBlock> Scheduler::markWaiting() {
        _currentTask->setTaskWaiting(true);
        return _currentTask;
    }

    void Scheduler::schedule() {
        _currentTask = _taskList;
        while (RBObject::NO_TASK != _currentTask) {
            if (_currentTask->isTaskHoldingOrWaiting()) {
                _currentTask = _currentTask->getLink();
            } else {
                _currentTaskIdentity = _currentTask->getIdentity();
                if (TRACING) {
                    trace(_currentTaskIdentity);
                }
                _currentTask = _currentTask->runTask();
            }
        }
    }
}