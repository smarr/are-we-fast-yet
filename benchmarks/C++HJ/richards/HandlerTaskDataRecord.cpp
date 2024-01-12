#include "HandlerTaskDataRecord.h"

using namespace std;

namespace richards {
    HandlerTaskDataRecord::HandlerTaskDataRecord() {
        _workIn = NO_WORK;
        _deviceIn = NO_WORK;
    }

    shared_ptr<Packet> HandlerTaskDataRecord::deviceIn() { 
        return _deviceIn; 
    }

    void HandlerTaskDataRecord::deviceIn(shared_ptr<Packet> aPacket) { 
        _deviceIn = aPacket;
    }

    void HandlerTaskDataRecord::deviceInAdd(shared_ptr<Packet> packet) {
        _deviceIn = append(packet, _deviceIn);
    }

    shared_ptr<Packet> HandlerTaskDataRecord::workIn() { 
        return _workIn; 
    }
    
    void HandlerTaskDataRecord::workIn(shared_ptr<Packet> aWorkQueue) { 
        _workIn = aWorkQueue;
    }

    void HandlerTaskDataRecord::workInAdd(shared_ptr<Packet> packet) {
        _workIn = append(packet, _workIn);
    }
}