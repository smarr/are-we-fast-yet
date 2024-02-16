#pragma once

#include "benchmark.h"
#include "memory/object_tracker.h"
#include "som/error.h"

#include <any>
#include <array>
#include <functional>
#include <iostream>
#include <string>

using std::cout;

class Packet;
class TaskControlBlock;

class RBObject : public TrackedObject {
 public:
  Packet* append(Packet* packet, Packet* queueHead);

  static const int32_t IDLER = 0;
  static const int32_t WORKER = 1;
  static const int32_t HANDLER_A = 2;
  static const int32_t HANDLER_B = 3;
  static const int32_t DEVICE_A = 4;
  static const int32_t DEVICE_B = 5;
  static const int32_t NUM_TYPES = 6;

  static const int32_t DEVICE_PACKET_KIND = 0;
  static const int32_t WORK_PACKET_KIND = 1;

  static constexpr Packet* const NO_WORK = nullptr;
  static constexpr TaskControlBlock* const NO_TASK = nullptr;
};

class TaskState : public RBObject {
 private:
  bool _packetPending{false};
  bool _taskWaiting{false};
  bool _taskHolding{false};

 public:
  [[nodiscard]] bool isPacketPending() const { return _packetPending; }
  [[nodiscard]] bool isTaskHolding() const { return _taskHolding; }
  [[nodiscard]] bool isTaskWaiting() const { return _taskWaiting; }
  void setTaskHolding(bool b) { _taskHolding = b; }
  void setTaskWaiting(bool b) { _taskWaiting = b; }
  void setPacketPending(bool b) { _packetPending = b; }

  void packetPending() {
    _packetPending = true;
    _taskWaiting = false;
    _taskHolding = false;
  }

  void running() { _packetPending = _taskWaiting = _taskHolding = false; }

  void waiting() {
    _packetPending = _taskHolding = false;
    _taskWaiting = true;
  }

  void waitingWithPacket() {
    _taskHolding = false;
    _taskWaiting = _packetPending = true;
  }

  [[nodiscard]] bool isTaskHoldingOrWaiting() const {
    return _taskHolding || (!_packetPending && _taskWaiting);
  }

  [[nodiscard]] bool isWaitingWithPacket() const {
    return _packetPending && _taskWaiting && !_taskHolding;
  }

  [[nodiscard]] static TaskState* createRunning() {
    auto* t = new TaskState();
    t->running();
    return t;
  }

  [[nodiscard]] static TaskState* createWaiting() {
    auto* t = new TaskState();
    t->waiting();
    return t;
  }

  [[nodiscard]] static TaskState* createWaitingWithPacket() {
    auto* t = new TaskState();
    t->waitingWithPacket();
    return t;
  }
};

class DeviceTaskDataRecord : public RBObject {
 private:
  Packet* _pending{NO_WORK};

 public:
  DeviceTaskDataRecord() = default;

  [[nodiscard]] Packet* getPending() const { return _pending; }
  void setPending(Packet* packet) { _pending = packet; }
};

class HandlerTaskDataRecord : public RBObject {
 private:
  Packet* _workIn{NO_WORK};
  Packet* _deviceIn{NO_WORK};

 public:
  HandlerTaskDataRecord() = default;

  Packet* deviceIn() { return _deviceIn; }
  void deviceIn(Packet* aPacket) { _deviceIn = aPacket; }
  void deviceInAdd(Packet* packet) { _deviceIn = append(packet, _deviceIn); }

  Packet* workIn() { return _workIn; }
  void workIn(Packet* aWorkQueue) { _workIn = aWorkQueue; }
  void workInAdd(Packet* packet) { _workIn = append(packet, _workIn); }
};

class IdleTaskDataRecord : public RBObject {
 private:
  int32_t _control{1};
  int32_t _count{10000};

 public:
  IdleTaskDataRecord() = default;

  [[nodiscard]] int32_t getControl() const { return _control; }
  void setControl(int32_t aNumber) { _control = aNumber; }
  [[nodiscard]] int32_t getCount() const { return _count; }
  void setCount(int32_t aCount) { _count = aCount; }
};

class Packet : public RBObject {
 public:
  static const int32_t DATA_SIZE = 4;

 private:
  Packet* _link;
  int32_t _identity;
  int32_t _kind;
  int32_t _datum{0};
  std::array<int32_t, Packet::DATA_SIZE> _data{0};

 public:
  Packet(Packet* link, int32_t identity, int32_t kind)
      : _link(link), _identity(identity), _kind(kind) {}

  [[nodiscard]] std::array<int32_t, Packet::DATA_SIZE>* getData() {
    return &_data;
  }

  [[nodiscard]] int32_t getDatum() const { return _datum; }
  void setDatum(int32_t someData) { _datum = someData; }
  [[nodiscard]] int32_t getIdentity() const { return _identity; }
  void setIdentity(int32_t anIdentity) { _identity = anIdentity; }
  [[nodiscard]] int32_t getKind() const { return _kind; }
  [[nodiscard]] Packet* getLink() { return _link; }
  void setLink(Packet* aLink) { _link = aLink; }

  [[nodiscard]] std::string toString() const {
    return "Packet id: " + std::to_string(_identity) +
           " kind: " + std::to_string(_kind);
  }
};

class TaskControlBlock : public TaskState {
 private:
  TaskControlBlock* _link;
  int32_t _identity;
  int32_t _priority;
  Packet* _input;
  std::function<TaskControlBlock*(Packet* work, RBObject* word)> _function;
  RBObject* _handle;

 public:
  TaskControlBlock(
      TaskControlBlock* aLink,
      int32_t anIdentity,
      int32_t aPriority,
      Packet* anInitialWorkQueue,
      TaskState* anInitialState,
      std::function<TaskControlBlock*(Packet* work, RBObject* word)> aBlock,
      RBObject* aPrivateData)
      : _link(aLink),
        _identity(anIdentity),
        _priority(aPriority),
        _input(anInitialWorkQueue),
        _function(std::move(aBlock)),
        _handle(aPrivateData) {
    setPacketPending(anInitialState->isPacketPending());
    setTaskWaiting(anInitialState->isTaskWaiting());
    setTaskHolding(anInitialState->isTaskHolding());
  }

  [[nodiscard]] int32_t getIdentity() const { return _identity; }
  [[nodiscard]] TaskControlBlock* getLink() { return _link; }
  [[nodiscard]] int32_t getPriority() const { return _priority; }

  [[nodiscard]] TaskControlBlock* addInputAndCheckPriority(
      Packet* packet,
      TaskControlBlock* oldTask) {
    if (NO_WORK == _input) {
      _input = packet;
      setPacketPending(true);
      if (_priority > oldTask->getPriority()) {
        return this;
      }
    } else {
      _input = append(packet, _input);
    }
    return oldTask;
  }

  [[nodiscard]] TaskControlBlock* runTask() {
    Packet* message = NO_WORK;

    if (isWaitingWithPacket()) {
      message = _input;
      _input = message->getLink();
      if (NO_WORK == _input) {
        running();
      } else {
        packetPending();
      }
    } else {
      message = NO_WORK;
    }
    return _function(message, _handle);
  }
};

class WorkerTaskDataRecord : public RBObject {
 private:
  int32_t _destination{RBObject::HANDLER_A};
  int32_t _count{0};

 public:
  WorkerTaskDataRecord() = default;

  [[nodiscard]] int32_t getCount() const { return _count; }
  void setCount(int32_t aCount) { _count = aCount; }
  [[nodiscard]] int32_t getDestination() const { return _destination; }
  void setDestination(int32_t aHandler) { _destination = aHandler; }
};

class Scheduler : public RBObject {
 private:
  TaskControlBlock* _taskList{NO_TASK};
  TaskControlBlock* _currentTask{NO_TASK};
  int32_t _currentTaskIdentity{0};
  std::array<TaskControlBlock*, RBObject::NUM_TYPES> _taskTable{NO_TASK};
  int32_t _queuePacketCount{0};
  int32_t _holdCount{0};
  int32_t _layout{0};
  static const bool TRACING = false;

 public:
  Scheduler() = default;

  void createDevice(int32_t identity,
                    int32_t priority,
                    Packet* workPacket,
                    TaskState* state) {
    auto* data = new DeviceTaskDataRecord();

    createTask(
        identity, priority, workPacket, state,
        [this](Packet* workArg, RBObject* wordArg) -> TaskControlBlock* {
          auto* dataRecord = dynamic_cast<DeviceTaskDataRecord*>(wordArg);
          Packet* functionWork = workArg;
          if (RBObject::NO_WORK == functionWork) {
            functionWork = dataRecord->getPending();
            if (RBObject::NO_WORK == functionWork) {
              return markWaiting();
            }
            dataRecord->setPending(RBObject::NO_WORK);
            return queuePacket(functionWork);
          }

          dataRecord->setPending(functionWork);
          if (TRACING) {
            trace(functionWork->getDatum());
          }
          return holdSelf();
        },
        data);
  }

  void createHandler(int32_t identity,
                     int32_t priority,
                     Packet* workPaket,
                     TaskState* state) {
    auto* data = new HandlerTaskDataRecord();

    createTask(
        identity, priority, workPaket, state,
        [this](Packet* work, RBObject* word) -> TaskControlBlock* {
          auto* dataRecord = dynamic_cast<HandlerTaskDataRecord*>(word);
          if (RBObject::NO_WORK != work) {
            if (WORK_PACKET_KIND == work->getKind()) {
              dataRecord->workInAdd(work);
            } else {
              dataRecord->deviceInAdd(work);
            }
          }

          Packet* workPacket = dataRecord->workIn();
          if (RBObject::NO_WORK == workPacket) {
            return markWaiting();
          }
          const int32_t count = workPacket->getDatum();
          if (count >= Packet::DATA_SIZE) {
            dataRecord->workIn(workPacket->getLink());
            return queuePacket(workPacket);
          }
          Packet* devicePacket = dataRecord->deviceIn();
          if (RBObject::NO_WORK == devicePacket) {
            return markWaiting();
          }
          dataRecord->deviceIn(devicePacket->getLink());
          devicePacket->setDatum(workPacket->getData()->at(count));
          workPacket->setDatum(count + 1);
          return queuePacket(devicePacket);
        },
        data);
  }

  void createIdler(int32_t identity,
                   int32_t priority,
                   Packet* work,
                   TaskState* state) {
    auto* data = new IdleTaskDataRecord();

    createTask(
        identity, priority, work, state,
        [this](Packet*, RBObject* wordArg) -> TaskControlBlock* {
          auto* dataRecord = dynamic_cast<IdleTaskDataRecord*>(wordArg);
          dataRecord->setCount(dataRecord->getCount() - 1);
          if (0 == dataRecord->getCount()) {
            return holdSelf();
          }
          if (0 == (dataRecord->getControl() & 1)) {
            dataRecord->setControl(dataRecord->getControl() / 2);
            return release(DEVICE_A);
          }
          dataRecord->setControl((dataRecord->getControl() / 2) ^ 53256);
          return release(DEVICE_B);
        },
        data);
  }

  Packet* createPacket(Packet* link, int32_t identity, int32_t kind) {
    return new Packet(link, identity, kind);
  }

  void createTask(
      int32_t identity,
      int32_t priority,
      Packet* work,
      TaskState* state,
      std::function<TaskControlBlock*(Packet* work, RBObject* word)> aBlock,
      RBObject* data) {
    auto* t = new TaskControlBlock(_taskList, identity, priority, work, state,
                                   std::move(aBlock), data);
    _taskList = t;
    _taskTable.at(identity) = t;
  }

  void createWorker(int32_t identity,
                    int32_t priority,
                    Packet* workPaket,
                    TaskState* state) {
    auto* dataRecord = new WorkerTaskDataRecord();

    createTask(
        identity, priority, workPaket, state,
        [this](Packet* work, RBObject* word) -> TaskControlBlock* {
          auto* data = dynamic_cast<WorkerTaskDataRecord*>(word);
          if (RBObject::NO_WORK == work) {
            return markWaiting();
          }
          data->setDestination(
              (HANDLER_A == data->getDestination()) ? HANDLER_B : HANDLER_A);
          work->setIdentity(data->getDestination());
          work->setDatum(0);
          for (int32_t i = 0; i < Packet::DATA_SIZE; i += 1) {
            data->setCount(data->getCount() + 1);
            if (data->getCount() > 26) {
              data->setCount(1);
            }
            work->getData()->at(i) = 65 + data->getCount() - 1;
          }
          return queuePacket(work);
        },
        dataRecord);
  }

  bool start() {
    Packet* workQ = RBObject::NO_WORK;
    createIdler(IDLER, 0, RBObject::NO_WORK, TaskState::createRunning());

    workQ = createPacket(RBObject::NO_WORK, WORKER, WORK_PACKET_KIND);
    workQ = createPacket(workQ, WORKER, WORK_PACKET_KIND);
    createWorker(WORKER, 1000, workQ, TaskState::createWaitingWithPacket());

    workQ = createPacket(RBObject::NO_WORK, DEVICE_A, DEVICE_PACKET_KIND);
    workQ = createPacket(workQ, DEVICE_A, DEVICE_PACKET_KIND);
    workQ = createPacket(workQ, DEVICE_A, DEVICE_PACKET_KIND);
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

  TaskControlBlock* findTask(int32_t identity) {
    TaskControlBlock* t = _taskTable.at(identity);
    if (RBObject::NO_TASK == t) {
      throw Error("findTask failed");
    }
    return t;
  }

  TaskControlBlock* holdSelf() {
    _holdCount = _holdCount + 1;
    _currentTask->setTaskHolding(true);
    return _currentTask->getLink();
  }

  TaskControlBlock* queuePacket(Packet* packet) {
    TaskControlBlock* t = findTask(packet->getIdentity());
    if (RBObject::NO_TASK == t) {
      return RBObject::NO_TASK;
    }

    _queuePacketCount = _queuePacketCount + 1;

    packet->setLink(RBObject::NO_WORK);
    packet->setIdentity(_currentTaskIdentity);
    return t->addInputAndCheckPriority(packet, _currentTask);
  }

  TaskControlBlock* release(int32_t identity) {
    TaskControlBlock* t = findTask(identity);
    if (RBObject::NO_TASK == t) {
      return RBObject::NO_TASK;
    }
    t->setTaskHolding(false);
    if (t->getPriority() > _currentTask->getPriority()) {
      return t;
    }
    return _currentTask;
  }

  void trace(int32_t id) {
    _layout = _layout - 1;
    if (0 >= _layout) {
      cout << "\n";
      _layout = 50;
    }
    cout << id << "\n";
  }

  TaskControlBlock* markWaiting() {
    _currentTask->setTaskWaiting(true);
    return _currentTask;
  }

  void schedule() {
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
};

class Richards : public Benchmark {
 public:
  bool verify_result(std::any result) override {
    const bool result_cast = std::any_cast<bool>(result);
    return result_cast;
  }

  std::any benchmark() override {
    auto* scheduler = new Scheduler();
    const bool result = scheduler->start();

    ObjectTracker::releaseAll();
    return result;
  }
};
