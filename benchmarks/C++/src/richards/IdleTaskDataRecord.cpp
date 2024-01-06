#include "IdleTaskDataRecord.h"

namespace richards {
    IdleTaskDataRecord::IdleTaskDataRecord() {
        _control = 1;
        _count = 10000;
    }
    
    int IdleTaskDataRecord::getControl() const {
        return _control; 
    }

    void IdleTaskDataRecord::setControl(int aNumber) {
        _control = aNumber;
    }

    int IdleTaskDataRecord::getCount() const {
        return _count; 
    }
        
    void IdleTaskDataRecord::setCount(int aCount) { 
        _count = aCount; 
    }
}

