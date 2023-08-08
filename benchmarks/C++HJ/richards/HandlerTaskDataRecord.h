#ifndef HANDLETASKDATARECORD
#define HANDLETASKDATARECORD

#include "Packet.h"
#include <memory>
#include <utility>

using namespace std;

namespace richards {
    class HandlerTaskDataRecord : public RBObject {
        private:

            shared_ptr<Packet> _workIn;
            shared_ptr<Packet> _deviceIn;

        public:

            HandlerTaskDataRecord();

            shared_ptr<Packet> deviceIn();
            void deviceIn(shared_ptr<Packet> aPacket);
            void deviceInAdd(shared_ptr<Packet> packet);
            shared_ptr<Packet> workIn();
            void workIn(shared_ptr<Packet> aWorkQueue);
            void workInAdd(shared_ptr<Packet> packet);
    };
}

#endif //HANDLETASKDATARECORD