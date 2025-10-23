use crate::richards::packet::PacketBox;
use crate::richards::rb_object::RBObject;
use crate::richards::scheduler::Scheduler;
use crate::richards::task_state::TaskState;
use std::cell::{Cell, RefCell};
use std::rc::Rc;

pub struct TaskControlBlock {
    link: Option<TaskControlBlockRc>,
    identity: usize,
    priority: usize,
    input: Cell<Option<PacketBox>>,
    state: TaskState,
    #[allow(clippy::type_complexity)]
    function: Box<RefCell<dyn FnMut(&Scheduler, Option<PacketBox>) -> Option<TaskControlBlockRc>>>,
}

#[allow(clippy::module_name_repetitions)]
pub type TaskControlBlockRc = Rc<TaskControlBlock>;

impl RBObject for TaskControlBlock {}

impl TaskControlBlock {
    pub fn new(
        link: Option<TaskControlBlockRc>,
        identity: usize,
        priority: usize,
        initial_input: Option<PacketBox>,
        initial_state: TaskState,
        function: impl 'static + FnMut(&Scheduler, Option<PacketBox>) -> Option<TaskControlBlockRc>,
    ) -> Self {
        Self {
            link,
            identity,
            priority,
            state: initial_state,
            input: Cell::new(initial_input),
            function: Box::new(RefCell::new(function)),
        }
    }

    pub fn new_rc(
        link: Option<TaskControlBlockRc>,
        identity: usize,
        priority: usize,
        an_initial_work_queue: Option<PacketBox>,
        an_initial_state: TaskState,
        a_block: impl 'static + FnMut(&Scheduler, Option<PacketBox>) -> Option<TaskControlBlockRc>,
    ) -> TaskControlBlockRc {
        Rc::new(Self::new(
            link,
            identity,
            priority,
            an_initial_work_queue,
            an_initial_state,
            a_block,
        ))
    }

    pub fn get_identity(&self) -> usize {
        self.identity
    }

    pub fn get_link(&self) -> &Option<TaskControlBlockRc> {
        &self.link
    }

    pub fn get_priority(&self) -> usize {
        self.priority
    }

    pub fn set_task_holding(&self, hold: bool) {
        self.state.set_task_holding(hold);
    }

    pub fn set_task_waiting(&self, wait: bool) {
        self.state.set_task_waiting(wait);
    }

    pub fn is_task_holding_or_waiting(&self) -> bool {
        self.state.is_task_holding_or_waiting()
    }

    pub fn add_input_and_check_priority(
        &self,
        packet: PacketBox,
        old_task: &TaskControlBlockRc,
        this_task: &TaskControlBlockRc,
    ) -> TaskControlBlockRc {
        let input = self.input.replace(None);
        if input.is_none() {
            self.input.set(Some(packet));
            self.state.set_packet_pending(true);
            if self.priority > old_task.get_priority() {
                return this_task.clone();
            }
        } else {
            self.input.set(Some(Self::append(packet, input)));
        }
        old_task.clone()
    }

    pub fn run_task(&self, scheduler: &Scheduler) -> Option<TaskControlBlockRc> {
        let message = if self.state.is_waiting_with_packet() {
            let mut message = self.input.replace(None);
            match message.as_mut().unwrap().take_link() {
                None => {
                    self.input.set(None);
                    self.state.running();
                }
                Some(input) => {
                    self.input.set(Some(input));
                    self.state.packet_pending();
                }
            }
            message
        } else {
            Self::NO_WORK
        };
        self.function.borrow_mut()(scheduler, message)
    }
}
