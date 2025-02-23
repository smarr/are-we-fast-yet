use crate::richards::rb_object::RBObject;
use std::cell::Cell;

#[derive(Clone)]
pub struct TaskState {
    packet_pending: Cell<bool>,
    task_waiting: Cell<bool>,
    task_holding: Cell<bool>,
}

impl Default for TaskState {
    fn default() -> Self {
        Self {
            packet_pending: Cell::new(true),
            task_waiting: Cell::new(false),
            task_holding: Cell::new(false),
        }
    }
}

impl RBObject for TaskState {}

impl TaskState {
    #[allow(dead_code)]
    pub fn is_packet_pending(&self) -> bool {
        self.packet_pending.get()
    }

    #[allow(dead_code)]
    pub fn is_task_holding(&self) -> bool {
        self.task_holding.get()
    }

    #[allow(dead_code)]
    pub fn is_task_waiting(&self) -> bool {
        self.task_waiting.get()
    }

    pub fn set_task_holding(&self, task_holding: bool) {
        self.task_holding.set(task_holding);
    }

    pub fn set_task_waiting(&self, task_waiting: bool) {
        self.task_waiting.set(task_waiting);
    }

    pub fn set_packet_pending(&self, packet_pending: bool) {
        self.packet_pending.set(packet_pending);
    }

    pub fn packet_pending(&self) {
        self.packet_pending.set(true);
        self.task_waiting.set(false);
        self.task_holding.set(false);
    }

    pub fn running(&self) {
        self.packet_pending.set(false);
        self.task_waiting.set(false);
        self.task_holding.set(false);
    }

    pub fn waiting(&self) {
        self.packet_pending.set(false);
        self.task_holding.set(false);
        self.task_waiting.set(true);
    }

    pub fn waiting_with_packet(&self) {
        self.task_holding.set(false);
        self.task_waiting.set(true);
        self.packet_pending.set(true);
    }

    #[allow(dead_code)]
    pub fn is_running(&self) -> bool {
        !self.packet_pending.get() && !self.task_waiting.get() && !self.task_holding.get()
    }

    pub fn is_task_holding_or_waiting(&self) -> bool {
        self.task_holding.get() || (!self.packet_pending.get() && self.task_waiting.get())
    }

    #[allow(dead_code)]
    pub fn is_waiting(&self) -> bool {
        !self.packet_pending.get() && self.task_waiting.get() && !self.task_holding.get()
    }

    pub fn is_waiting_with_packet(&self) -> bool {
        self.packet_pending.get() && self.task_waiting.get() && !self.task_holding.get()
    }

    #[allow(dead_code)]
    pub fn create_packet_pending() -> TaskState {
        let task_state = TaskState::default();
        task_state.packet_pending();
        task_state
    }

    pub fn create_running() -> TaskState {
        let task_state = TaskState::default();
        task_state.running();
        task_state
    }

    pub fn create_waiting() -> TaskState {
        let task_state = TaskState::default();
        task_state.waiting();
        task_state
    }

    pub fn create_waiting_with_packet() -> TaskState {
        let task_state = TaskState::default();
        task_state.waiting_with_packet();
        task_state
    }
}
