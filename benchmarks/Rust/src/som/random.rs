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
        self.seed = ((self.seed * 1309) + 13849) & 65535;
        self.seed
    }
}

#[cfg(test)]
mod tests {
    use crate::som::random::Random;

    #[test]
    fn random_tests() {
        let mut random = Random::default();
        assert_eq!(random.next(), 22896);
        assert_eq!(random.next(), 34761);
        assert_eq!(random.next(), 34014);
        assert_eq!(random.next(), 39231);
        assert_eq!(random.next(), 52540);
        assert_eq!(random.next(), 41445);
        assert_eq!(random.next(), 1546);
        assert_eq!(random.next(), 5947);
        assert_eq!(random.next(), 65224);
    }
}
