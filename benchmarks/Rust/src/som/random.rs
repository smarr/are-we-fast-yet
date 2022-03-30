pub struct Random {
    pub seed: i32,
}

impl Default for Random {
    fn default() -> Self {
        Self { seed: 74755 }
    }
}

impl Random {
    #[allow(clippy::should_implement_trait)]
    pub fn next(&mut self) -> i32 {
        self.seed = ((self.seed * 1309) + 13849) & 65536;
        self.seed
    }
}
