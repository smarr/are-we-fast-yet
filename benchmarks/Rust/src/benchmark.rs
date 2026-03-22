use std::any::Any;

pub trait Benchmark {
    fn benchmark(&self) -> Box<dyn Any>;
    fn verify_result(&self, result: Box<dyn Any>) -> bool;

    fn inner_benchmark_loop(&self, inner_iterations: usize) -> bool {
        for _ in 0..inner_iterations {
            if !self.verify_result(self.benchmark()) {
                return false;
            }
        }
        true
    }
}
