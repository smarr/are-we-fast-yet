use crate::richards::packet::PacketBox;
use crate::richards::task_control_block::TaskControlBlock;

pub trait RBObject {
    fn append(mut packet: PacketBox, queue_head: Option<PacketBox>) -> PacketBox {
        packet.set_link(Self::NO_WORK);
        match queue_head {
            None => packet,
            Some(mut queue_head) => {
                queue_head.append_link(Some(packet));
                queue_head
            }
        }
    }

    const IDLER: usize = 0;
    const WORKER: usize = 1;
    const HANDLER_A: usize = 2;
    const HANDLER_B: usize = 3;
    const DEVICE_A: usize = 4;
    const DEVICE_B: usize = 5;
    const NUM_TYPES: usize = 6;

    const DEVICE_PACKET_KIND: usize = 0;
    const WORK_PACKET_KIND: usize = 1;

    const NO_WORK: Option<PacketBox> = None;
    const NO_TASK: Option<TaskControlBlock> = None;
}
