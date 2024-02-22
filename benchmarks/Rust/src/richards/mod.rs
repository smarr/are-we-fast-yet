mod device_task_data_record;
mod handler_task_data_record;
mod idle_task_data_record;
mod packet;
mod rb_object;
mod scheduler;
mod task_control_block;
mod task_state;
mod worker_task_data_record;

use crate::benchmark::Benchmark;
use crate::richards::scheduler::Scheduler;
use std::any::Any;

#[derive(Default)]
pub struct Richards;

impl Benchmark for Richards {
    fn benchmark(&self) -> Box<dyn Any> {
        Box::new(Scheduler::default().start())
    }

    fn verify_result(&self, result: Box<dyn Any>) -> bool {
        if let Ok(result) = result.downcast::<bool>() {
            *result
        } else {
            false
        }
    }
}
