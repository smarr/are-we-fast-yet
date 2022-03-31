use crate::benchmark::Benchmark;
use crate::json::Json;
use crate::nbody::NBody;
use crate::richards::Richards;
use crate::sieve::Sieve;
use crate::storage::Storage;
use crate::towers::Towers;
use std::time::Duration;

pub struct Run {
    name: String,
    benchmark_suite: Box<dyn Benchmark>,
    num_iterations: usize,
    inner_iterations: usize,
    total: Duration,
}

impl Run {
    #[must_use]
    pub fn new(name: String) -> Run {
        let benchmark_suite = Self::get_suite_from_name(&name);
        Run {
            name,
            benchmark_suite,
            num_iterations: 1,
            inner_iterations: 1,
            total: Duration::new(0, 0),
        }
    }

    fn get_suite_from_name(name: &str) -> Box<dyn Benchmark> {
        match name {
            "Json" => Box::new(Json::default()),
            "NBody" => Box::new(NBody::default()),
            "Richards" => Box::new(Richards::default()),
            "Sieve" => Box::new(Sieve::default()),
            "Storage" => Box::new(Storage::default()),
            "Towers" => Box::new(Towers::default()),
            _ => panic!("Unknown benchmark suite"),
        }
    }

    pub fn run_benchmark(&mut self) {
        println!("Starting {} benchmark ...", self.name);
        self.do_runs();
        self.report_benchmark();
        println!();
    }

    fn measure(&mut self) {
        let start_time = std::time::Instant::now();
        if !(self
            .benchmark_suite
            .inner_benchmark_loop(self.inner_iterations))
        {
            panic!("Benchmark failed with incorrect result");
        }
        let end_time = std::time::Instant::now();
        let run_time = end_time - start_time;
        self.print_result(&run_time);
        self.total += run_time;
    }

    fn do_runs(&mut self) {
        (0..self.num_iterations).for_each(|_| self.measure());
    }

    fn report_benchmark(&self) {
        let Run {
            name,
            num_iterations,
            ..
        } = self;
        let total = self.total.as_micros();
        let average = total / *num_iterations as u128;
        println!("{name}: iterations={num_iterations} average: {average}us total:{total}us\n");
    }

    fn print_result(&self, run_time: &Duration) {
        let name = self.name.as_str();
        let run_time = run_time.as_micros();
        println!("{name}: iterations=1 runtime: {run_time}us");
    }

    pub fn print_total(&self) {
        let total = self.total.as_micros();
        println!("Total Runtime: {total}us");
    }

    pub fn set_num_iterations(&mut self, num_iterations: usize) {
        self.num_iterations = num_iterations;
    }

    pub fn set_inner_iterations(&mut self, inner_iterations: usize) {
        self.inner_iterations = inner_iterations;
    }
}
