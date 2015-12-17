import java.util.Arrays;

import som.Random;


public final class Bounce extends Benchmark {

  private static class Ball {
    private int x;
    private int y;
    private int xVel;
    private int yVel;

    public Ball(final Random random) {
      x = random.next() % 500;
      y = random.next() % 500;
      xVel = (random.next() % 300) - 150;
      yVel = (random.next() % 300) - 150;
    }

    public boolean bounce() {
      int xLimit = 500;
      int yLimit = 500;
      boolean bounced = false;

      x += xVel;
      y += yVel;
      if (x > xLimit) { x = xLimit; xVel = 0 - Math.abs(xVel); bounced = true; }
      if (x < 0)      { x = 0;      xVel = Math.abs(xVel);     bounced = true; }
      if (y > yLimit) { y = yLimit; yVel = 0 - Math.abs(yVel); bounced = true; }
      if (y < 0)      { y = 0;      yVel = Math.abs(yVel);     bounced = true; }
      return bounced;
    }
  }

  @Override
  public Object benchmark() {
    Random random = new Random();

    int ballCount = 100;
    int bounces   = 0;
    Ball[] balls  = new Ball[ballCount];

    Arrays.setAll(balls, v -> new Ball(random));

    for (int i = 1; i <= 50; i++) {
      for (Ball ball : balls) {
        if (ball.bounce()) {
          bounces++;
        }
      }
    }
    return bounces;
  }

  @Override
  public boolean verifyResult(final Object result) {
    return 1331 == (int) result;
  }
}
