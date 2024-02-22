use crate::benchmark::Benchmark;
use std::any::Any;

pub struct Queens {
    free_rows: [bool; 8],
    free_maxs: [bool; 16],
    free_mins: [bool; 16],
    queen_rows: [usize; 8],
}

impl Default for Queens {
    fn default() -> Self {
        Queens {
            free_rows: [true; 8],
            free_maxs: [true; 16],
            free_mins: [true; 16],
            queen_rows: [usize::MAX; 8],
        }
    }
}

impl Benchmark for Queens {
    fn benchmark(&self) -> Box<dyn Any> {
        let mut result = true;
        for _ in 0..10 {
            result = result && Queens::default().queens();
        }
        Box::new(result)
    }

    fn verify_result(&self, result: Box<dyn Any>) -> bool {
        if let Ok(result) = result.downcast::<bool>() {
            *result
        } else {
            false
        }
    }
}

impl Queens {
    fn queens(&mut self) -> bool {
        self.place_queen(0)
    }

    fn place_queen(&mut self, c: usize) -> bool {
        for r in 0..8 {
            if self.get_row_column(r, c) {
                self.queen_rows[r as usize] = c;
                self.set_row_column(r, c, false);

                if c == 7 || self.place_queen(c + 1) {
                    return true;
                }
                self.set_row_column(r, c, true);
            }
        }
        false
    }

    fn get_row_column(&self, r: usize, c: usize) -> bool {
        self.free_rows[r as usize]
            && self.free_maxs[(c + r) as usize]
            && self.free_mins[(c - r + 7) as usize]
    }

    fn set_row_column(&mut self, r: usize, c: usize, v: bool) {
        self.free_rows[r as usize] = v;
        self.free_maxs[(c + r) as usize] = v;
        self.free_mins[(c - r + 7) as usize] = v;
    }
}
