
#ifndef RBOBJECT
#define RBOBJECT


#include <memory>

using namespace std;

namespace richards {
    class Packet;
    class TaskControlBlock;

    class RBObject {
        public:
            virtual ~RBObject();
            
            shared_ptr<Packet> append(shared_ptr<Packet> packet, shared_ptr<Packet> queueHead);
            static void initializeConstants();

            static const int IDLER = 0;
            static const int WORKER = 1;
            static const int HANDLER_A = 2;
            static const int HANDLER_B = 3;
            static const int DEVICE_A = 4;
            static const int DEVICE_B = 5;
            static const int NUM_TYPES = 6;

            static const int DEVICE_PACKET_KIND = 0;
            static const int WORK_PACKET_KIND = 1;

            static shared_ptr<Packet> NO_WORK;
            static shared_ptr<TaskControlBlock> NO_TASK;
    };
}

#endif //RBOBJECT