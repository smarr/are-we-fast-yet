#ifndef DEVICETASKDATARECORD
#define DEVICETASKDATARECORD


#include "Packet.h"
#include <memory>
#include <utility>

using namespace std;

namespace richards {
    class DeviceTaskDataRecord : public RBObject {
        private:
            shared_ptr<Packet> _pending;

        public:
            DeviceTaskDataRecord();

            shared_ptr<Packet> getPending();
            void setPending(shared_ptr<Packet> packet);
    };
}

#endif //DEVICETASKDATARECORD