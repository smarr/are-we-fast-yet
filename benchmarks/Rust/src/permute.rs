use crate::benchmark::Benchmark;
use std::any::Any;

#[derive(Default)]
pub struct Permute {
    count: usize,
    v: [usize; 6],
}

impl Benchmark for Permute {
    fn benchmark(&self) -> Box<dyn Any> {
        let mut permute = Permute::default();
        permute.permute(6);
        Box::new(permute.count)
    }

    fn verify_result(&self, result: Box<dyn Any>) -> bool {
        if let Ok(result) = result.downcast::<usize>() {
            *result == 8660
        } else {
            false
        }
    }
}

impl Permute {
    fn permute(&mut self, n: usize) {
        self.count += 1;
        if n != 0 {
            let n1 = n - 1;
            self.permute(n1);
            for i in (0..=n1).rev() {
                self.swap(n1, i);
                self.permute(n1);
                self.swap(n1, i);
            }
        }
    }

    fn swap(&mut self, i: usize, j: usize) {
        self.v.swap(i, j);
    }
}
