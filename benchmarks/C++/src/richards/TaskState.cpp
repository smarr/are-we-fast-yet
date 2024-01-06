#include "TaskState.h"

namespace richards {        
    bool TaskState::isPacketPending() const {
        return _packetPending; 
    }

    bool TaskState::isTaskHolding() const {
        return _taskHolding;   
    }

    bool TaskState::isTaskWaiting() const {
        return _taskWaiting;   
    }

    void TaskState::setTaskHolding(bool b) { 
        _taskHolding = b; 
    }

    void TaskState::setTaskWaiting(bool b) { 
        _taskWaiting = b; 
    }

    void TaskState::setPacketPending(bool b) { 
        _packetPending = b; 
    }

    void TaskState::packetPending() {
        _packetPending = true;
        _taskWaiting = false;
        _taskHolding = false;
    }

    void TaskState::running() {
        _packetPending = _taskWaiting = _taskHolding = false;
    }

    void TaskState::waiting() {
        _packetPending = _taskHolding = false;
        _taskWaiting = true;
    }

    void TaskState::waitingWithPacket() {
        _taskHolding = false;
        _taskWaiting = _packetPending = true;
    }

    bool TaskState::isTaskHoldingOrWaiting() const {
        return _taskHolding || (!_packetPending && _taskWaiting);
    }

    bool TaskState::isWaitingWithPacket() const {
        return _packetPending && _taskWaiting && !_taskHolding;
    }

    std::shared_ptr<TaskState> TaskState::createRunning() {
        std::shared_ptr<TaskState> t = std::make_shared<TaskState>();
        t->running();
        return t;
    }

    std::shared_ptr<TaskState> TaskState::createWaiting() {
        std::shared_ptr<TaskState> t = std::make_shared<TaskState>();
        t->waiting();
        return t;
    }

    std::shared_ptr<TaskState> TaskState::createWaitingWithPacket() {
        std::shared_ptr<TaskState> t = std::make_shared<TaskState>();
        t->waitingWithPacket();
        return t;
    }
};