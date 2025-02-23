use crate::benchmark::Benchmark;
use std::any::Any;

#[derive(Default)]
pub struct Mandelbrot;

impl Benchmark for Mandelbrot {
    fn benchmark(&self) -> Box<dyn Any> {
        unimplemented!("Should never be reached")
    }

    fn verify_result(&self, _result: Box<dyn Any>) -> bool {
        unimplemented!("Should never be reached")
    }

    fn inner_benchmark_loop(&self, inner_iterations: usize) -> bool {
        #[allow(clippy::cast_possible_truncation, clippy::cast_possible_wrap)]
        let inner_iterations = inner_iterations as i32;
        Self::verify_inner_result(Self::mandelbrot(inner_iterations), inner_iterations)
    }
}

impl Mandelbrot {
    fn verify_inner_result(result: i32, inner_iterations: i32) -> bool {
        match inner_iterations {
            500 => result == 191,
            750 => result == 50,
            1 => result == 128,
            _ => {
                println!("No verification result for {inner_iterations} found");
                println!("Result is: {result}");
                false
            }
        }
    }

    fn mandelbrot(size: i32) -> i32 {
        let size = size as f64;
        let mut sum = 0;
        let mut byte_acc = 0;
        let mut bit_num = 0;

        let mut y = 0.0;

        while y < size {
            let ci = (2.0 * y / size) - 1.0;
            let mut x = 0.0;

            while x < size {
                let mut zrzr = 0.0;
                let mut zi = 0.0;
                let mut zizi = 0.0;
                let cr = (2.0 * x / size) - 1.5;

                let mut z = 0;
                let mut not_done = true;
                let mut escape = 0;
                while not_done && z < 50 {
                    let zr = zrzr - zizi + cr;
                    zi = 2.0 * zr * zi + ci;

                    // preserve recalculation
                    zrzr = zr * zr;
                    zizi = zi * zi;

                    if zrzr + zizi > 4.0 {
                        not_done = false;
                        escape = 1;
                    }
                    z += 1;
                }

                byte_acc = (byte_acc << 1) + escape;
                bit_num += 1;

                // Code is very similar for these cases, but using separate blocks
                // ensures we skip the shifting when it's unnecessary, which is most cases.
                if bit_num == 8 {
                    sum ^= byte_acc;
                    byte_acc = 0;
                    bit_num = 0;
                } else if x == size - 1.0 {
                    byte_acc <<= 8 - bit_num;
                    sum ^= byte_acc;
                    byte_acc = 0;
                    bit_num = 0;
                }
                x += 1.0;
            }
            y += 1.0;
        }
        sum
    }
}
