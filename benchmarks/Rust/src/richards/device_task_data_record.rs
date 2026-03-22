use crate::richards::packet::PacketBox;

#[derive(Default)]
pub struct DeviceTaskDataRecord {
    pending: Option<PacketBox>,
}

impl DeviceTaskDataRecord {
    pub fn take_pending(&mut self) -> Option<PacketBox> {
        self.pending.take()
    }

    pub fn set_pending(&mut self, packet: Option<PacketBox>) {
        self.pending = packet;
    }
}
