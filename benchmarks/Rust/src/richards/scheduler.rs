use crate::richards::device_task_data_record::DeviceTaskDataRecord;
use crate::richards::handler_task_data_record::HandlerTaskDataRecord;
use crate::richards::idle_task_data_record::IdleTaskDataRecord;
use crate::richards::packet::{Packet, PacketBox};
use crate::richards::rb_object::RBObject;
use crate::richards::task_control_block::{TaskControlBlock, TaskControlBlockRc};
use crate::richards::task_state::TaskState;
use crate::richards::worker_task_data_record::WorkerTaskDataRecord;
use std::cell::Cell;

pub struct Scheduler {
    task_list: Option<TaskControlBlockRc>,
    current_task: Option<TaskControlBlockRc>,
    current_task_identity: usize,
    task_table: [Option<TaskControlBlockRc>; TaskControlBlock::NUM_TYPES],
    queue_packet_count: Cell<usize>,
    hold_count: Cell<usize>,
    layout: Cell<isize>,
}

impl Default for Scheduler {
    fn default() -> Self {
        Self {
            // init tracing
            layout: Cell::new(0),

            // init scheduler
            queue_packet_count: Cell::new(0),
            hold_count: Cell::new(0),
            task_table: Default::default(),

            // init task list
            current_task: None,
            current_task_identity: 0,
            task_list: None,
        }
    }
}

impl RBObject for Scheduler {}

impl Scheduler {
    const TRACING: bool = false;

    fn create_device(
        &mut self,
        identity: usize,
        priority: usize,
        work_packet: Option<PacketBox>,
        state: TaskState,
    ) {
        let mut data = DeviceTaskDataRecord::default();
        self.create_task(
            identity,
            priority,
            work_packet,
            state,
            move |sched, function_work| match function_work {
                None => match data.take_pending() {
                    None => sched.mark_waiting(),
                    Some(function_work) => {
                        data.set_pending(Packet::NO_WORK);
                        sched.queue_packet(function_work)
                    }
                },
                Some(function_work) => {
                    if Self::TRACING {
                        sched.trace(function_work.get_datum());
                    }
                    data.set_pending(Some(function_work));
                    sched.hold_self()
                }
            },
        );
    }

    fn create_handler(
        &mut self,
        identity: usize,
        priority: usize,
        work_packet: Option<PacketBox>,
        state: TaskState,
    ) {
        let mut data = HandlerTaskDataRecord::default();
        self.create_task(
            identity,
            priority,
            work_packet,
            state,
            move |sched, work| {
                if let Some(work) = work {
                    if Packet::WORK_PACKET_KIND == work.get_kind() {
                        data.work_in_add(work);
                    } else {
                        data.device_in_add(work);
                    }
                }

                match data.take_work_in() {
                    None => sched.mark_waiting(),
                    Some(mut work) => {
                        let count = work.get_datum();
                        if count >= Packet::DATA_SIZE {
                            data.set_work_in(work.take_link());
                            sched.queue_packet(work)
                        } else {
                            match data.take_device_in() {
                                None => {
                                    data.set_work_in(Some(work));
                                    sched.mark_waiting()
                                }
                                Some(mut device_packet) => {
                                    data.set_device_in(device_packet.take_link());
                                    device_packet.set_datum(work.get_data()[count]);
                                    work.set_datum(count + 1);
                                    data.set_work_in(Some(work));
                                    sched.queue_packet(device_packet)
                                }
                            }
                        }
                    }
                }
            },
        );
    }

    fn create_idler(
        &mut self,
        identity: usize,
        priority: usize,
        work: Option<PacketBox>,
        state: TaskState,
    ) {
        let mut data = IdleTaskDataRecord::default();
        self.create_task(identity, priority, work, state, move |sched, _work| {
            data.set_count(data.get_count() - 1);
            if 0 == data.get_count() {
                sched.hold_self()
            } else if 0 == data.get_control() & 1 {
                data.set_control(data.get_control() / 2);
                sched.release(Packet::DEVICE_A)
            } else {
                data.set_control((data.get_control() / 2) ^ 53256);
                sched.release(Packet::DEVICE_B)
            }
        });
    }

    fn create_packet(link: Option<PacketBox>, identity: usize, kind: usize) -> PacketBox {
        Packet::new_boxed(link, identity, kind)
    }

    fn create_task(
        &mut self,
        identity: usize,
        priority: usize,
        work: Option<PacketBox>,
        state: TaskState,
        a_block: impl 'static + FnMut(&Scheduler, Option<PacketBox>) -> Option<TaskControlBlockRc>,
    ) {
        let t = TaskControlBlock::new_rc(
            self.task_list.take(),
            identity,
            priority,
            work,
            state,
            a_block,
        );
        self.task_list = Some(t.clone());
        self.task_table[identity] = Some(t);
    }

    fn create_worker(
        &mut self,
        identity: usize,
        priority: usize,
        work_packet: Option<PacketBox>,
        state: TaskState,
    ) {
        let mut data = WorkerTaskDataRecord::default();
        self.create_task(
            identity,
            priority,
            work_packet,
            state,
            move |sched, work| match work {
                None => sched.mark_waiting(),
                Some(mut work) => {
                    data.set_destination(
                        if WorkerTaskDataRecord::HANDLER_A == data.get_destination() {
                            WorkerTaskDataRecord::HANDLER_B
                        } else {
                            WorkerTaskDataRecord::HANDLER_A
                        },
                    );
                    work.set_identity(data.get_destination());
                    work.set_datum(0);
                    for i in 0..Packet::DATA_SIZE {
                        data.set_count(data.get_count() + 1);
                        if data.get_count() > 26 {
                            data.set_count(1);
                        }
                        work.get_data_mut()[i] = 65 + data.get_count() - 1;
                    }
                    sched.queue_packet(work)
                }
            },
        );
    }

    pub fn start(&mut self) -> bool {
        self.create_idler(Self::IDLER, 0, Self::NO_WORK, TaskState::create_running());
        let mut work_q = Self::create_packet(Self::NO_WORK, Self::WORKER, Self::WORK_PACKET_KIND);
        work_q = Self::create_packet(Some(work_q), Self::WORKER, Self::WORK_PACKET_KIND);

        self.create_worker(
            Self::WORKER,
            1000,
            Some(work_q),
            TaskState::create_waiting_with_packet(),
        );
        work_q = Self::create_packet(Self::NO_WORK, Self::DEVICE_A, Self::DEVICE_PACKET_KIND);
        work_q = Self::create_packet(Some(work_q), Self::DEVICE_A, Self::DEVICE_PACKET_KIND);
        work_q = Self::create_packet(Some(work_q), Self::DEVICE_A, Self::DEVICE_PACKET_KIND);

        self.create_handler(
            Self::HANDLER_A,
            2000,
            Some(work_q),
            TaskState::create_waiting_with_packet(),
        );
        work_q = Self::create_packet(Self::NO_WORK, Self::DEVICE_B, Self::DEVICE_PACKET_KIND);
        work_q = Self::create_packet(Some(work_q), Self::DEVICE_B, Self::DEVICE_PACKET_KIND);
        work_q = Self::create_packet(Some(work_q), Self::DEVICE_B, Self::DEVICE_PACKET_KIND);

        self.create_handler(
            Self::HANDLER_B,
            3000,
            Some(work_q),
            TaskState::create_waiting_with_packet(),
        );
        self.create_device(
            Self::DEVICE_A,
            4000,
            Self::NO_WORK,
            TaskState::create_waiting(),
        );
        self.create_device(
            Self::DEVICE_B,
            5000,
            Self::NO_WORK,
            TaskState::create_waiting(),
        );

        self.schedule();

        self.queue_packet_count.get() == 23246 && self.hold_count.get() == 9297
    }

    fn find_task(&self, identity: usize) -> &TaskControlBlockRc {
        self.task_table[identity]
            .as_ref()
            .expect("find_task failed")
    }

    fn hold_self(&self) -> Option<TaskControlBlockRc> {
        self.hold_count.set(self.hold_count.get() + 1);
        if let Some(current_task) = &self.current_task {
            current_task.set_task_holding(true);
            current_task.get_link().as_ref().cloned()
        } else {
            panic!("hold_self failed")
        }
    }

    #[allow(clippy::unnecessary_wraps)]
    fn queue_packet(&self, mut packet: PacketBox) -> Option<TaskControlBlockRc> {
        self.queue_packet_count
            .set(self.queue_packet_count.get() + 1);
        let t = self.find_task(packet.get_identity());
        packet.set_link(Packet::NO_WORK);
        packet.set_identity(self.current_task_identity);
        Some(t.add_input_and_check_priority(packet, self.current_task.as_ref().unwrap(), t))
    }

    fn release(&self, identity: usize) -> Option<TaskControlBlockRc> {
        let t = self.find_task(identity);
        t.set_task_holding(false);
        if t.get_priority() > self.current_task.as_ref().unwrap().get_priority() {
            Some(t.clone())
        } else {
            self.current_task.clone()
        }
    }

    fn trace(&self, id: usize) {
        self.layout.set(self.layout.get() - 1);
        if 0 >= self.layout.get() {
            println!();
            self.layout.set(50);
        }
        println!("{id}");
    }

    fn mark_waiting(&self) -> Option<TaskControlBlockRc> {
        self.current_task.as_ref().unwrap().set_task_waiting(true);
        self.current_task.clone()
    }

    fn schedule(&mut self) {
        self.current_task = self.task_list.clone();
        while let Some(current_task) = &self.current_task {
            if current_task.is_task_holding_or_waiting() {
                self.current_task = current_task.get_link().as_ref().cloned();
            } else {
                self.current_task_identity = current_task.get_identity();
                if Self::TRACING {
                    self.trace(current_task.get_identity());
                }
                self.current_task = current_task.run_task(self);
            }
        }
    }
}
