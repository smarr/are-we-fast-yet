use crate::benchmark::Benchmark;
use crate::som::random::Random;
use std::any::Any;

#[derive(Default)]
pub struct Bounce;

impl Benchmark for Bounce {
    #[allow(clippy::items_after_statements)]
    fn benchmark(&self) -> Box<dyn Any> {
        let mut random = Random::default();

        const BALL_COUNT: usize = 100;
        let mut bounces = 0usize;
        let mut balls = [Ball::default(); BALL_COUNT];
        for ball in &mut balls {
            *ball = Ball::new(&mut random);
        }

        for _i in 0..50 {
            for ball in &mut balls {
                if ball.bounce() {
                    bounces += 1;
                }
            }
        }
        Box::new(bounces)
    }

    fn verify_result(&self, result: Box<dyn Any>) -> bool {
        if let Ok(result) = result.downcast::<usize>() {
            1331 == *result
        } else {
            false
        }
    }
}

#[derive(Default, Debug, Clone, Copy)]
struct Ball {
    x: i32,
    y: i32,
    x_vel: i32,
    y_vel: i32,
}

impl Ball {
    fn new(random: &mut Random) -> Self {
        let x = random.next() % 500;
        let y = random.next() % 500;
        let x_vel = (random.next() % 300) - 150;
        let y_vel = (random.next() % 300) - 150;
        Ball { x, y, x_vel, y_vel }
    }

    fn bounce(&mut self) -> bool {
        let x_limit = 500;
        let y_limit = 500;
        let mut bounced = false;

        self.x += self.x_vel;
        self.y += self.y_vel;
        if self.x > x_limit {
            self.x = x_limit;
            self.x_vel = 0 - self.x_vel.abs();
            bounced = true;
        }
        if self.x < 0 {
            self.x = 0;
            self.x_vel = self.x_vel.abs();
            bounced = true;
        }
        if self.y > y_limit {
            self.y = y_limit;
            self.y_vel = 0 - self.y_vel.abs();
            bounced = true;
        }
        if self.y < 0 {
            self.y = 0;
            self.y_vel = self.y_vel.abs();
            bounced = true;
        }
        bounced
    }
}
