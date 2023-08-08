#include "Packet.h"


namespace richards {
    
    shared_ptr<Packet> RBObject::NO_WORK = nullptr;
    shared_ptr<TaskControlBlock> RBObject::NO_TASK = nullptr;
    
    void RBObject::initializeConstants() {
        NO_WORK = nullptr;
        NO_TASK = nullptr;
    };

    std::shared_ptr<Packet> RBObject::append(std::shared_ptr<Packet> packet, std::shared_ptr<Packet> queueHead) {
        packet->setLink(NO_WORK);
        if (NO_WORK == queueHead) {
            return packet;
        }

        std::shared_ptr<Packet> mouse = queueHead;
        std::shared_ptr<Packet> link;
        while (NO_WORK != (link = mouse->getLink())) {
            mouse = link;
        }
        mouse->setLink(packet);
        return queueHead;
    }
    
    RBObject::~RBObject() = default;

}

