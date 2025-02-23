use crate::richards::rb_object::RBObject;

pub struct WorkerTaskDataRecord {
    destination: usize,
    count: usize,
}

impl Default for WorkerTaskDataRecord {
    fn default() -> Self {
        Self {
            destination: Self::HANDLER_A,
            count: 0,
        }
    }
}

impl RBObject for WorkerTaskDataRecord {}

impl WorkerTaskDataRecord {
    pub fn get_count(&self) -> usize {
        self.count
    }

    pub fn set_count(&mut self, count: usize) {
        self.count = count;
    }

    pub fn get_destination(&self) -> usize {
        self.destination
    }

    pub fn set_destination(&mut self, destination: usize) {
        self.destination = destination;
    }
}
