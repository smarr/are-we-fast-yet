use crate::richards::rb_object::RBObject;

pub struct IdleTaskDataRecord {
    control: usize,
    count: usize,
}

impl Default for IdleTaskDataRecord {
    fn default() -> Self {
        Self {
            control: 1,
            count: 10000,
        }
    }
}

impl RBObject for IdleTaskDataRecord {}

impl IdleTaskDataRecord {
    pub fn get_control(&self) -> usize {
        self.control
    }

    pub fn set_control(&mut self, control: usize) {
        self.control = control;
    }

    pub fn get_count(&self) -> usize {
        self.count
    }

    pub fn set_count(&mut self, count: usize) {
        self.count = count;
    }
}
