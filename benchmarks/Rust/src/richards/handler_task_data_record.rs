use crate::richards::packet::{Packet, PacketBox};
use crate::richards::rb_object::RBObject;

pub struct HandlerTaskDataRecord {
    work_in: Option<PacketBox>,
    device_in: Option<PacketBox>,
}

impl Default for HandlerTaskDataRecord {
    fn default() -> Self {
        Self {
            work_in: Packet::NO_WORK,
            device_in: Packet::NO_WORK,
        }
    }
}

impl RBObject for HandlerTaskDataRecord {}

impl HandlerTaskDataRecord {
    pub fn take_device_in(&mut self) -> Option<PacketBox> {
        self.device_in.take()
    }

    pub fn set_device_in(&mut self, device_in: Option<PacketBox>) {
        self.device_in = device_in;
    }

    pub fn device_in_add(&mut self, packet: PacketBox) {
        self.device_in = Some(Self::append(packet, self.device_in.take()));
    }

    pub fn take_work_in(&mut self) -> Option<PacketBox> {
        self.work_in.take()
    }

    pub fn set_work_in(&mut self, work_in: Option<PacketBox>) {
        self.work_in = work_in;
    }

    pub fn work_in_add(&mut self, packet: PacketBox) {
        self.work_in = Some(Self::append(packet, self.work_in.take()));
    }
}
