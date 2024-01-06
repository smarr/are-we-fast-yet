#include "richards.h"

Packet* RBObject::append(Packet* packet, Packet* queueHead) {
  packet->setLink(NO_WORK);
  if (NO_WORK == queueHead) {
    return packet;
  }

  Packet* mouse = queueHead;
  Packet* link = NO_WORK;
  while (NO_WORK != (link = mouse->getLink())) {
    mouse = link;
  }
  mouse->setLink(packet);
  return queueHead;
}
