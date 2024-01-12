#include "DeviceTaskDataRecord.h"

using namespace std;

namespace richards {

    DeviceTaskDataRecord::DeviceTaskDataRecord() {
        _pending = NO_WORK;
    }

    shared_ptr<Packet> DeviceTaskDataRecord::getPending() {
        return _pending; 
    }
    void DeviceTaskDataRecord::setPending(shared_ptr<Packet> packet) {
        _pending = packet;
    }
}