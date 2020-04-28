class Random:
    seed = 74755;

    def next(self):
        self.seed = ((self.seed * 1309) + 13849) & 65535

        return self.seed

def abs(i):
    if (i < 0):
        return -1 * i
    return i

class Ball:
    x = 0
    y = 0
    xVel = 0
    yVel = 0

    def __init__(self, r):
        self.x = r.next() % 500
        self.y = r.next() % 500
        self.xVel = (r.next() % 300) - 150
        self.yVel = (r.next() % 300) - 150

    def bounce(self):
        xLimit = 500
        yLimit = 500
        bounced = False

        self.x += self.xVel
        self.y += self.yVel

        if (self.x > xLimit):
            self.x = xLimit
            self.xVel = 0 - abs(self.xVel)
            bounced = True

        if (self.x < 0):
            self.x = 0
            self.xVel = abs(self.xVel)
            bounced = True

        if (self.y > yLimit):
            self.y = yLimit
            self.yVel = 0 - abs(self.yVel)
            bounced = True

        if (self.y < 0):
            self.y = 0
            self.yVel = abs(self.yVel)
            bounced = True

        return bounced

r = Random()

ballCount = 100
bounces   = 0
balls = []

for i in range(0, ballCount):
    balls.append(Ball(r))

for i in range(0, 50):
    for b in range(0, ballCount):
        ball = balls[b]

        if (ball.bounce()):
          bounces += 1

print(bounces)
