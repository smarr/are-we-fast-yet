/*
 * Copyright (c) 2023 Rochus Keller <me@rochus-keller.ch> (for C++ migration)
 *
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * Richards.
 *
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */

#include "Richards.h"
#include "Object.h"
#include <string>
#include <sstream>
#include <iostream>

class Packet;
class TaskControlBlock;

static Packet* const NO_WORK = 0;
static TaskControlBlock* const NO_TASK = 0;


// bare pointers no delete and no instance counting: 3357us
// bare pointers no delete with counting instances: 3553us and 3200 remaining objs
// without addRef()/release() calls: 3654us and 3200 remaining objs
// with full ref/release and instance counting: 4008us and 0 remaining objs
// with full ref/release and no instance counting: 3787us
// so ref counting costs (3787-3357)/3357 ~13%

// compared with 2'880us for pure C implementation with Boehm GC

class RBObject : public Object {
public:
    static Packet* append(Packet* packet, Packet* queueHead);

    enum { IDLER     = 0,
           WORKER    = 1,
           HANDLER_A = 2,
           HANDLER_B = 3,
           DEVICE_A  = 4,
           DEVICE_B  = 5,
           NUM_TYPES = 6 };

    enum { DEVICE_PACKET_KIND = 0, WORK_PACKET_KIND   = 1 };
};

class Packet : public RBObject {
public:
    enum { DATA_SIZE = 4 };

private:
    Ref<Packet> link;
    int identity;
    int kind;
    int datum;
    int data[DATA_SIZE];

public:

    Packet(Packet* link, int identity, int kind) {
        this->link     = link;
        this->identity = identity;
        this->kind     = kind;
        this->datum    = 0;
        for( int i = 0; i < DATA_SIZE; i++ )
            data[i] = 0;
    }


    int* getData() { return data; }
    int   getDatum() { return datum; }
    void  setDatum(int someData) { datum = someData; }

    int  getIdentity() { return identity; }
    void setIdentity(int anIdentity) { identity = anIdentity; }

    int getKind() { return kind; }
    Packet* getLink() { return link; }
    void setLink(Packet* aLink) { link = aLink; }

    std::string toString() {
        std::ostringstream tmp;
        tmp << "Packet id: " << identity << " kind: " << kind;
        return tmp.str();
    }
};

Packet *RBObject::append(Packet *packet, Packet *queueHead)
{
    packet->setLink(NO_WORK);
    if (NO_WORK == queueHead) {
        return packet;
    }

    Ref<Packet> mouse = queueHead;
    Ref<Packet> link;
    while (NO_WORK != (link = mouse->getLink())) {
        mouse = link;
    }
    mouse->setLink(packet);
    return queueHead;
}

class TaskState : public RBObject {
    bool packetPending_;
    bool taskWaiting;
    bool taskHolding;

public:
    TaskState():packetPending_(false),taskWaiting(false),taskHolding(false){}

    bool isPacketPending() { return packetPending_; }
    bool isTaskHolding()   { return taskHolding;   }
    bool isTaskWaiting()   { return taskWaiting;   }

    void setTaskHolding(bool b) { taskHolding = b; }
    void setTaskWaiting(bool b) { taskWaiting = b; }
    void setPacketPending(bool b) { packetPending_ = b; }

    void packetPending() {
        packetPending_ = true;
        taskWaiting   = false;
        taskHolding   = false;
    }

    void running() {
        packetPending_ = taskWaiting = taskHolding = false;
    }

    void waiting() {
        packetPending_ = taskHolding = false;
        taskWaiting = true;
    }

    void waitingWithPacket() {
        taskHolding = false;
        taskWaiting = packetPending_ = true;
    }

    bool isRunning() {
        return !packetPending_ && !taskWaiting && !taskHolding;
    }

    bool isTaskHoldingOrWaiting() {
        return taskHolding || (!packetPending_ && taskWaiting);
    }

    bool isWaiting() {
        return !packetPending_ && taskWaiting && !taskHolding;
    }

    bool isWaitingWithPacket() {
        return packetPending_ && taskWaiting && !taskHolding;
    }

    static Ref<TaskState> createPacketPending() {
        TaskState* t = new TaskState();
        t->packetPending();
        return t;
    }

    static Ref<TaskState> createRunning() {
        TaskState* t = new TaskState();
        t->running();
        return t;
    }

    static Ref<TaskState> createWaiting() {
        TaskState* t = new TaskState();
        t->waiting();
        return t;
    }

    static Ref<TaskState> createWaitingWithPacket() {
        TaskState* t = new TaskState();
        t->waitingWithPacket();
        return t;
    }
};

class ProcessFunction : public RBObject {
public:
    virtual TaskControlBlock* apply(Packet* work, RBObject* word) = 0;
};

class TaskControlBlock : public TaskState {
    Ref<TaskControlBlock> link;
    int identity;
    int priority;
    Ref<Packet> input;
    Ref<ProcessFunction> function;
    Ref<RBObject> handle;

public:
    TaskControlBlock(TaskControlBlock* aLink, int anIdentity,
                     int aPriority, Packet* anInitialWorkQueue,
                     TaskState* anInitialState, ProcessFunction* aBlock,
                     RBObject* aPrivateData) {
        link = aLink;
        identity = anIdentity;
        priority = aPriority;
        input = anInitialWorkQueue;
        setPacketPending(anInitialState->isPacketPending());
        setTaskWaiting(anInitialState->isTaskWaiting());
        setTaskHolding(anInitialState->isTaskHolding());
        function = aBlock;
        handle = aPrivateData;
    }

    int getIdentity() { return identity; }
    TaskControlBlock* getLink()  { return link; }
    int getPriority() { return priority; }

    TaskControlBlock* addInputAndCheckPriority(Packet* packet,
                                               TaskControlBlock* oldTask) {
        if (NO_WORK == input) {
            input = packet;
            setPacketPending(true);
            if (priority > oldTask->getPriority()) { return this; }
        } else {
            input = append(packet, input);
        }
        return oldTask;
    }

    TaskControlBlock* runTask() {
        Ref<Packet> message;
        if (isWaitingWithPacket()) {
            message = input;
            input = message->getLink();
            if (NO_WORK == input) {
                running();
            } else {
                packetPending();
            }
        } else {
            message = NO_WORK;
        }
        return function->apply(message, handle);
    }
};

class DeviceTaskDataRecord : public RBObject {
    Ref<Packet> pending;

public:
    DeviceTaskDataRecord() {
        pending = NO_WORK;
    }

    Packet* getPending() { return pending; }
    void setPending(Packet* packet) { pending = packet; }
};

class HandlerTaskDataRecord : public RBObject {
    Ref<Packet> workIn_;
    Ref<Packet> deviceIn_;

public:
    HandlerTaskDataRecord() {
        workIn_ = deviceIn_ = NO_WORK;
    }

    Packet* deviceIn() { return deviceIn_; }
    void deviceIn(Packet* aPacket) { deviceIn_ = aPacket; }

    void deviceInAdd(Packet* packet) {
        deviceIn_ = append(packet, deviceIn_);
    }

    Packet* workIn() { return workIn_; }
    void workIn(Packet* aWorkQueue) { workIn_ = aWorkQueue; }

    void workInAdd(Packet* packet) {
        workIn_ = append(packet, workIn_);
    }
};

class IdleTaskDataRecord : public RBObject {
    int control;
    int count;

public:
    int getControl() { return control; }
    void setControl(int aNumber) {
        control = aNumber;
    }

    int getCount() { return count; }
    void setCount(int aCount) { count = aCount; }

    IdleTaskDataRecord() {
        control = 1;
        count = 10000;
    }
};

class WorkerTaskDataRecord : public RBObject {
  int destination;
  int count;

public:
  WorkerTaskDataRecord() {
    destination = HANDLER_A;
    count = 0;
  }

  int getCount() { return count; }
  void setCount(int aCount) { count = aCount; }

  int getDestination() { return destination; }
  void setDestination(int aHandler) { destination = aHandler; }
};

static bool TRACING = false;

class Scheduler : public RBObject {
  Ref<TaskControlBlock> taskList;
  Ref<TaskControlBlock> currentTask;
  int currentTaskIdentity;
  Ref<TaskControlBlock> taskTable[NUM_TYPES];

  int queuePacketCount;
  int holdCount;

  int layout;

public:
  Scheduler() {
    // init tracing
    layout  = 0;
    currentTask = 0;
    currentTaskIdentity = 0;

    // init scheduler
    queuePacketCount = 0;
    holdCount = 0;
    for(int i = 0; i < NUM_TYPES; i++ )
        taskTable[i] = NO_TASK;
    taskList = NO_TASK;
  }


  void createDevice(int identity, int priority, Packet* workPacket, TaskState* state) {
      Ref<DeviceTaskDataRecord> data = new DeviceTaskDataRecord();

      class FP : public ProcessFunction
      {
          Scheduler* s;
      public:
          FP(Scheduler* s_):s(s_){}
          TaskControlBlock* apply(Packet* workArg, RBObject* wordArg)
          {
              DeviceTaskDataRecord* dataRecord = dynamic_cast<DeviceTaskDataRecord*>(wordArg);
              Ref<Packet> functionWork = workArg;
              if (NO_WORK == functionWork) {
                  if (NO_WORK == (functionWork = dataRecord->getPending())) {
                      return s->markWaiting();
                  } else {
                      dataRecord->setPending(NO_WORK);
                      return s->queuePacket(functionWork);
                  }
              } else {
                  dataRecord->setPending(functionWork);
                  if (TRACING) {
                      s->trace(functionWork->getDatum());
                  }
                  return s->holdSelf();
              }
          }
      };
      Ref<ProcessFunction> fp = new FP(this);
      createTask(identity, priority, workPacket, state, fp, data);
  }

  void createHandler(int identity, int priority, Packet* workPaket, TaskState* state) {
      Ref<HandlerTaskDataRecord> data = new HandlerTaskDataRecord();

      class FP : public ProcessFunction
      {
          Scheduler* s;
      public:
          FP(Scheduler* s_):s(s_){}
          TaskControlBlock* apply(Packet* work, RBObject* word)
          {
              HandlerTaskDataRecord* dataRecord = dynamic_cast<HandlerTaskDataRecord*>(word);
              if (NO_WORK != work) {
                  if (WORK_PACKET_KIND == work->getKind()) {
                      dataRecord->workInAdd(work);
                  } else {
                      dataRecord->deviceInAdd(work);
                  }
              }

              Ref<Packet> workPacket;
              if (NO_WORK == (workPacket = dataRecord->workIn())) {
                  return s->markWaiting();
              } else {
                  int count = workPacket->getDatum();
                  if (count >= Packet::DATA_SIZE) {
                      dataRecord->workIn(workPacket->getLink());
                      return s->queuePacket(workPacket);
                  } else {
                      Ref<Packet> devicePacket;
                      if (NO_WORK == (devicePacket = dataRecord->deviceIn())) {
                          return s->markWaiting();
                      } else {
                          dataRecord->deviceIn(devicePacket->getLink());
                          devicePacket->setDatum(workPacket->getData()[count]);
                          workPacket->setDatum(count + 1);
                          return s->queuePacket(devicePacket);
                      }
                  }
              }
          }
      };

      Ref<ProcessFunction> fp = new FP(this);
      createTask(identity, priority, workPaket, state, fp, data);
  }

  void createIdler(int identity, int priority, Packet* work, TaskState* state) {

      Ref<IdleTaskDataRecord> data = new IdleTaskDataRecord();

      class FP : public ProcessFunction
      {
          Scheduler* s;
      public:
          FP(Scheduler* s_):s(s_){}
          TaskControlBlock* apply(Packet* workArg, RBObject* wordArg)
          {
              IdleTaskDataRecord* dataRecord = dynamic_cast<IdleTaskDataRecord*>(wordArg);
              dataRecord->setCount(dataRecord->getCount() - 1);
              if (0 == dataRecord->getCount()) {
                  return s->holdSelf();
              } else {
                  if (0 == (dataRecord->getControl() & 1)) {
                      dataRecord->setControl(dataRecord->getControl() / 2);
                      return s->release(DEVICE_A);
                  } else {
                      dataRecord->setControl((dataRecord->getControl() / 2) ^ 53256);
                      return s->release(DEVICE_B);
                  }
              }
          }
      };
      Ref<ProcessFunction> fp = new FP(this);
      createTask(identity, priority, work, state, fp, data);
  }

  Ref<Packet> createPacket(Packet* link, int identity, int kind) {
      return new Packet(link, identity, kind);
  }

  void createTask(int identity, int priority, Packet* work, TaskState* state,
                  ProcessFunction* aBlock, RBObject* data) {

      Ref<TaskControlBlock> t = new TaskControlBlock(taskList, identity,
                                                 priority, work, state, aBlock, data);
      taskList = t;
      taskTable[identity] = t;
  }

  void createWorker(int identity, int priority, Packet* workPaket, TaskState* state) {
      Ref<WorkerTaskDataRecord> dataRecord = new WorkerTaskDataRecord();

      class FP : public ProcessFunction
      {
          Scheduler* s;
      public:
          FP(Scheduler* s_):s(s_){}
          TaskControlBlock* apply(Packet* work, RBObject* word)
          {
              WorkerTaskDataRecord* data = dynamic_cast<WorkerTaskDataRecord*>(word);
              if (NO_WORK == work) {
                  return s->markWaiting();
              } else {
                  data->setDestination((HANDLER_A == data->getDestination()) ? HANDLER_B : HANDLER_A);
                  work->setIdentity(data->getDestination());
                  work->setDatum(0);
                  for (int i = 0; i < Packet::DATA_SIZE; i++) {
                      data->setCount(data->getCount() + 1);
                      if (data->getCount() > 26) { data->setCount(1); }
                      work->getData()[i] = 65 + data->getCount() - 1;
                  }
                  return s->queuePacket(work);
              }
          }
      };

      Ref<ProcessFunction> fp = new FP(this);
      createTask(identity, priority, workPaket, state, fp, dataRecord);
  }

  bool start() {
      Ref<Packet> workQ;

      createIdler(IDLER, 0, NO_WORK, TaskState::createRunning());
      workQ = createPacket(NO_WORK, WORKER, WORK_PACKET_KIND);
      workQ = createPacket(workQ,   WORKER, WORK_PACKET_KIND);

      createWorker(WORKER, 1000, workQ, TaskState::createWaitingWithPacket());
      workQ = createPacket(NO_WORK, DEVICE_A, DEVICE_PACKET_KIND);
      workQ = createPacket(workQ,   DEVICE_A, DEVICE_PACKET_KIND);
      workQ = createPacket(workQ,   DEVICE_A, DEVICE_PACKET_KIND);

      createHandler(HANDLER_A, 2000, workQ, TaskState::createWaitingWithPacket());
      workQ = createPacket(NO_WORK, DEVICE_B, DEVICE_PACKET_KIND);
      workQ = createPacket(workQ, DEVICE_B, DEVICE_PACKET_KIND);
      workQ = createPacket(workQ, DEVICE_B, DEVICE_PACKET_KIND);

      createHandler(HANDLER_B, 3000, workQ, TaskState::createWaitingWithPacket());
      createDevice(DEVICE_A, 4000, NO_WORK, TaskState::createWaiting());
      createDevice(DEVICE_B, 5000, NO_WORK, TaskState::createWaiting());

      schedule();

      return queuePacketCount == 23246 && holdCount == 9297;
  }

  TaskControlBlock* findTask(int identity) {
      TaskControlBlock* t = taskTable[identity];
      if (NO_TASK == t) { throw "findTask failed"; }
      return t;
  }

  TaskControlBlock* holdSelf() {
      holdCount = holdCount + 1;
      currentTask->setTaskHolding(true);
      return currentTask->getLink();
  }

  TaskControlBlock* queuePacket(Packet* packet) {
      TaskControlBlock* t = findTask(packet->getIdentity());
      if (NO_TASK == t) { return NO_TASK; }

      queuePacketCount = queuePacketCount + 1;

      packet->setLink(NO_WORK);
      packet->setIdentity(currentTaskIdentity);
      return t->addInputAndCheckPriority(packet, currentTask);
  }

  TaskControlBlock* release(int identity) {
      TaskControlBlock* t = findTask(identity);
      if (NO_TASK == t) { return NO_TASK; }
      t->setTaskHolding(false);
      if (t->getPriority() > currentTask->getPriority()) {
          return t;
      } else {
          return currentTask;
      }
  }

  void trace(int id) {
      layout = layout - 1;
      if (0 >= layout) {
          // Checkstyle: stop
          std::cout << std::endl;
          // Checkstyle: resume
          layout = 50;
      }
      // Checkstyle: stop
      std::cout << id << std::flush;
      // Checkstyle: resume
  }

  TaskControlBlock* markWaiting() {
      currentTask->setTaskWaiting(true);
      return currentTask;
  }

  void schedule() {
      currentTask = taskList;
      while (NO_TASK != currentTask) {
          if (currentTask->isTaskHoldingOrWaiting()) {
              currentTask = currentTask->getLink();
          } else {
              currentTaskIdentity = currentTask->getIdentity();
              if (TRACING) { trace(currentTaskIdentity); }
              currentTask = currentTask->runTask();
          }
      }
  }
};

int Richards::benchmark()
{
    bool res = false;
    {
        Scheduler s;
        res = s.start();
    }
    return res;
}

