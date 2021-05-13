class Random:
    seed = 74755

    def next(self):
        self.seed = ((self.seed * 1309) + 13849) & 65535

        return self.seed


class Ball:
    x = 0
    y = 0
    x_vel = 0
    y_vel = 0

    def __init__(self, r):
        self.x = r.next() % 500
        self.y = r.next() % 500
        self.x_vel = (r.next() % 300) - 150
        self.y_vel = (r.next() % 300) - 150

    def bounce(self):
        x_limit = 500
        y_limit = 500
        bounced = False

        self.x += self.x_vel
        self.y += self.y_vel

        if self.x > x_limit:
            self.x = x_limit
            self.x_vel = 0 - abs(self.x_vel)
            bounced = True

        if self.x < 0:
            self.x = 0
            self.x_vel = abs(self.x_vel)
            bounced = True

        if self.y > y_limit:
            self.y = y_limit
            self.y_vel = 0 - abs(self.y_vel)
            bounced = True

        if self.y < 0:
            self.y = 0
            self.y_vel = abs(self.y_vel)
            bounced = True

        return bounced


r = Random()

ball_count = 100
bounces = 0
balls = []

for i in range(0, ball_count):
    balls.append(Ball(r))

for i in range(0, 50):
    for b in range(0, ball_count):
        ball = balls[b]

        if ball.bounce():
            bounces += 1

print(bounces)
