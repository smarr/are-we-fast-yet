use crate::benchmark::Benchmark;
use std::any::Any;

#[derive(Default)]
pub struct Sieve;

impl Benchmark for Sieve {
    fn benchmark(&self) -> Box<dyn Any> {
        let mut flags = vec![true; 10000];
        Box::new(Self::sieve(&mut flags, 5000))
    }

    fn verify_result(&self, result: Box<dyn Any>) -> bool {
        if let Some(result) = result.downcast_ref::<usize>() {
            669 == *result
        } else {
            false
        }
    }
}

impl Sieve {
    fn sieve(mut flags: &mut [bool], size: usize) -> usize {
        let mut prime_count = 0;
        flags = &mut flags[1..(size - 1)]; // Select the range we want to touch

        // This dance is because we are wanting to access multiple parts of the array mutably
        // simultaneously.  Would be more simple if we relied on the officials `itertools` crate,
        // but doing it manually here for simplicity.
        let mut i = 1;
        while let Some((flag_i_1, flags_i)) = flags.split_first_mut() {
            i += 1;
            if *flag_i_1 {
                prime_count += 1;
                flags_i
                    .iter_mut()
                    .skip(i - 1)
                    .step_by(i)
                    .for_each(|flag| *flag = false);
            }
            flags = flags_i;
        }

        prime_count
    }
}
