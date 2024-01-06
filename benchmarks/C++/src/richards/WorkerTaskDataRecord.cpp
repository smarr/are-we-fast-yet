#include "WorkerTaskDataRecord.h"

namespace richards {
    WorkerTaskDataRecord::WorkerTaskDataRecord() {
        _destination = HANDLER_A;
        _count = 0;
    }
        
    int WorkerTaskDataRecord::getCount() const {
        return _count; 
    }

    void WorkerTaskDataRecord::setCount(int aCount) { 
        _count = aCount; 
    }

    int WorkerTaskDataRecord::getDestination() const {
        return _destination; 
    }

    void WorkerTaskDataRecord::setDestination(int aHandler) { 
        _destination = aHandler; 
    }
}