use crate::benchmark::Benchmark;
use crate::som::random::Random;
use std::any::Any;

#[derive(Default)]
pub struct Storage {
    count: i32,
}

impl Benchmark for Storage {
    fn benchmark(&self) -> Box<dyn Any> {
        let mut random = Random::default();
        let mut run = Self::default();
        run.build_tree_depth(7, &mut random);
        Box::new(run.count)
    }

    fn verify_result(&self, result: Box<dyn Any>) -> bool {
        if let Ok(count) = result.downcast::<i32>() {
            5461 == *count
        } else {
            false
        }
    }
}

impl Storage {
    fn build_tree_depth(&mut self, depth: i32, random: &mut Random) -> Box<dyn Any> {
        self.count += 1;
        if depth == 1 {
            Box::new([random.next() % 10 + 1])
        } else {
            let mut build = || self.build_tree_depth(depth - 1, random);
            Box::new([build(), build(), build(), build()])
        }
    }
}
