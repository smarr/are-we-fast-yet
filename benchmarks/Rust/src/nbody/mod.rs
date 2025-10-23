mod body;
mod nbody_system;

use crate::benchmark::Benchmark;
use crate::nbody::nbody_system::NBodySystem;
use std::any::Any;

#[derive(Default)]
pub struct NBody;

impl Benchmark for NBody {
    fn benchmark(&self) -> Box<dyn Any> {
        unimplemented!("Should never be reached")
    }

    fn verify_result(&self, _result: Box<dyn Any>) -> bool {
        unimplemented!("Should never be reached")
    }

    fn inner_benchmark_loop(&self, inner_iterations: usize) -> bool {
        let mut system = NBodySystem::default();
        for _ in 0..inner_iterations {
            system.advance(0.01);
        }

        Self::verify_result_direct(system.energy(), inner_iterations)
    }
}

impl NBody {
    fn verify_result_direct(result: f64, inner_iterations: usize) -> bool {
        #[allow(clippy::unreadable_literal, clippy::float_cmp)]
        match inner_iterations {
            250_000 => return result == -0.1690859889909308,
            1 => return result == -0.16907495402506745,
            _ => (),
        }
        println!("No verification result for {inner_iterations} found");
        println!("Result is: {result}");
        false
    }
}
